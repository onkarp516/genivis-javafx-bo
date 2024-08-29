package com.opethic.genivis.controller.Reports;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.opethic.genivis.dto.Reports.PurchaseOrderDTO;
import com.opethic.genivis.dto.reqres.product.Communicator;
import com.opethic.genivis.network.APIClient;
import com.opethic.genivis.network.EndPoints;
import com.opethic.genivis.network.RequestType;
import com.opethic.genivis.utils.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;

import static com.opethic.genivis.utils.FxmFileConstants.PURCHASE_ORDER_YEARLY_SLUG;

public class PurchaseOrderController implements Initializable {
    @FXML
    private BorderPane bpPurOrderRootPane;
    @FXML
    private ComboBox<String> cmbPurOrderSuplInv, cmbPurOrderDuration;
    @FXML
    private TextField tfPurOrderListSearch, tfPurOrderListFromDate, tfPurOrderListToDate, tfFromAmt, tfToAmt;
    @FXML
    private Button btReset, btExportPdf, btExportExcel, btExportCsv, btExportPrint;
    @FXML
    private TableView<PurchaseOrderDTO> tblPurOrderList;
    @FXML
    private TableColumn<PurchaseOrderDTO, String> tblPurOrderDate, tblPurOrderLedger, tblPurOrderVoucherType, tblPurOrderVoucherNo, tblPurOrderDebitAmt, tblPurOrderCreditAmt;
    @FXML
    private Label lblTotalDebit, lblTotalCredit;

    String duration = "";
    String selectedDuration = "", selectedFilter = "", selectedType = "";

    private ObservableList<PurchaseOrderDTO> originalData;

    String message = "";
    private JsonObject jsonObject = null;

    public static final Logger purchaseOrderLogger = LoggerFactory.getLogger(PurchaseOrderController.class);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> {
            tfPurOrderListSearch.requestFocus();
        });

        // Initial Static Values Set Supplier/Voucher  Combobox
        cmbPurOrderSuplInv.getItems().addAll("Supplier", "Voucher");
        cmbPurOrderSuplInv.setPromptText("Filter Search");
        cmbPurOrderSuplInv.setOnAction(this::handleFilterComboBoxAction);
        cmbPurOrderSuplInv.setValue("Supplier");
        handleFilterComboBoxAction(null);
        //todo:open Filter dropdown on Space
        cmbPurOrderSuplInv.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.SPACE && !cmbPurOrderSuplInv.isShowing()) {
                cmbPurOrderSuplInv.show();
                event.consume();
            }
        });

        // Initial Static Values Set Duration  Combobox
        cmbPurOrderDuration.getItems().addAll("This Month", "Last Month", "Half Year", "Full Year");
        cmbPurOrderDuration.setPromptText("Select Duration");
        cmbPurOrderDuration.setOnAction(this::handleDurationComboBoxAction);
        cmbPurOrderDuration.setValue(cmbPurOrderDuration.getItems().get(0));
        duration = "month";
        //todo:open Filter dropdown on Space
        cmbPurOrderDuration.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.SPACE && !cmbPurOrderDuration.isShowing()) {
                cmbPurOrderDuration.show();
                event.consume();
            }
        });

        getPurchaseOrderData();

        // Amountwise Filter after changing amount in the ToAMount field
        tfToAmt.textProperty().addListener((observable, oldValue, newValue) -> {
            filterAmountData();
        });

        btReset.setOnAction(e -> {
            resetFields();
        });

        // TextFields Converting in DateField
        DateValidator.applyDateFormat(tfPurOrderListFromDate);
        DateValidator.applyDateFormat(tfPurOrderListToDate);

        // Setting the handler for each node
        setKeyPressedHandler(cmbPurOrderSuplInv);
        setKeyPressedHandler(tfPurOrderListSearch);
        setKeyPressedHandler(cmbPurOrderDuration);
        setKeyPressedHandler(tfPurOrderListFromDate);
        setKeyPressedHandler(tfPurOrderListToDate);
        setKeyPressedHandler(tfFromAmt);
        setKeyPressedHandler(tfToAmt);
        setKeyPressedHandler(btReset);
        setKeyPressedHandler(btExportPdf);
        setKeyPressedHandler(btExportExcel);
        setKeyPressedHandler(btExportCsv);
        setKeyPressedHandler(btExportPrint);
        tblPurOrderList.requestFocus();

        tfPurOrderListToDate.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                System.out.println("DateFilter From Date " + tfPurOrderListFromDate.getText().toString());
                System.out.println("DateFilter To Date " + tfPurOrderListToDate.getText().toString());
                if (!tfPurOrderListFromDate.getText().equalsIgnoreCase("") && !tfPurOrderListToDate.getText().equalsIgnoreCase("")) {
                    getPurchaseOrderData();
                }
                Node nextNode = CommonTraversa.getNextFocusableNode(tfFromAmt.getScene());
                if (nextNode != null) {
                    nextNode.requestFocus();
                }
            }
            if (event.getCode() == KeyCode.TAB) {
                if (!tfPurOrderListFromDate.getText().equalsIgnoreCase("") && !tfPurOrderListToDate.getText().equalsIgnoreCase("")) {
                    getPurchaseOrderData();
                }
            }
        });

        btReset.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> updateButtonStyle(isNowFocused || btReset.isHover()));
        btReset.hoverProperty().addListener((obs, wasHovered, isNowHovered) -> updateButtonStyle(isNowHovered || btReset.isFocused()));
    }

    // Method to handle key pressed event for any Node
    private void setKeyPressedHandler(Node node) {
        node.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                Node nextNode = CommonTraversa.getNextFocusableNode(node.getScene());
                if (nextNode != null) {
                    nextNode.requestFocus();
                }
            }
        });
    }

    private void updateButtonStyle(boolean isActive) {
        if (isActive) {
            btReset.setStyle("-fx-border-width: 2; -fx-border-color: #00a0f5;");
        } else {
            btReset.setStyle("-fx-border-width: 0;");
        }
    }

    private void filterAmountData() {
        purchaseOrderLogger.debug("Get filterAmountData Started...");
        String fromAmountText = tfFromAmt.getText();
        String toAmountText = tfToAmt.getText();
        if (toAmountText.isEmpty()) {
            tblPurOrderList.setItems(FXCollections.observableList(originalData));
            calculateTotalAmount();
            return;
        }

        double fromAmount;
        double toAmount;
        String message = "Please Enter a Valid Amount Range.";
        String message1 = "Please Enter Valid Numeric Amounts.";
        Stage stage2 = (Stage) bpPurOrderRootPane.getScene().getWindow();
        AlertUtility.CustomCallback callback = (number) -> {
            if (number == 1) {
            }
        };
        try {
            fromAmount = Double.parseDouble(fromAmountText);
            toAmount = Double.parseDouble(toAmountText);
        } catch (NumberFormatException e) {
            purchaseOrderLogger.error("Get filterAmountData NumberFormatException...");
            AlertUtility.AlertError(stage2, AlertUtility.alertTypeError, message1, callback);
            return;
        }

        List<PurchaseOrderDTO> filteredData = new ArrayList<>();
        for (PurchaseOrderDTO purOrderData : originalData) {
            Double dobCred = Double.parseDouble(purOrderData.getCredit());
            Double dobDeb = Double.parseDouble(purOrderData.getDebit());
            if ((dobCred >= fromAmount && dobCred <= toAmount) || (dobDeb >= fromAmount && dobDeb <= toAmount)) {
                purchaseOrderLogger.debug("Get filterAmountData SuccessFully Executed...");
                filteredData.add(purOrderData);
            }

        }

        tblPurOrderList.setItems(FXCollections.observableList(filteredData));
        calculateTotalAmount();
        purchaseOrderLogger.debug("Get filterAmountData End...");
    }

    private void getPurchaseOrderData() {
        APIClient apiClient = null;
        purchaseOrderLogger.debug("Get getPurchaseOrderData Started...");
        Map<String, String> body = new HashMap<>();
        try {
            System.out.println("selectedDuration --->>> " + duration);
            if (!duration.isEmpty()) {
                body.put("duration", duration);
            } else if (!tfPurOrderListFromDate.getText().equalsIgnoreCase("") && !tfPurOrderListToDate.getText().equalsIgnoreCase("")) {
                System.out.println("date sdf ");
                body.put("start_date", Communicator.text_to_date.fromString(tfPurOrderListFromDate.getText()).toString());
                body.put("end_date", Communicator.text_to_date.fromString(tfPurOrderListToDate.getText()).toString());
            }
            System.out.println("body --->>> " + body);
            String formData = Globals.mapToStringforFormData(body);
            apiClient = new APIClient(EndPoints.getPurchaseOrderData, formData, RequestType.FORM_DATA);
            apiClient.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    jsonObject = new Gson().fromJson(workerStateEvent.getSource().getValue().toString(), JsonObject.class);
                    System.out.println("response =======> " + jsonObject);
                    ObservableList<PurchaseOrderDTO> observableList = FXCollections.observableArrayList();
                    if (jsonObject.get("responseStatus").getAsInt() == 200) {
                        purchaseOrderLogger.debug("Get getPurchaseOrderData Success...");
                        JsonArray responseArray = jsonObject.get("debit").getAsJsonArray();
//                        System.out.println("responseArray--------> " + responseArray);
                        String st_dt = (jsonObject.get("d_start_date").getAsString());
                        LocalDate start_date = LocalDate.parse(st_dt);
                        String en_dt = jsonObject.get("d_end_date").getAsString();
                        LocalDate end_date = LocalDate.parse(en_dt);

                        tfPurOrderListFromDate.setText(start_date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                        tfPurOrderListToDate.setText(end_date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

                        if (responseArray.size() > 0) {
                            for (JsonElement element : responseArray) {
                                JsonObject item = element.getAsJsonObject();
                                String id = item.get("row_id").getAsString();
                                String date = item.get("transaction_date").getAsString();
                                String ledger = item.get("particulars").getAsString();
                                String voucherType = item.get("voucher_type").getAsString();
                                String voucherNo = item.get("voucher_no").getAsString();
                                String debit = item.get("debit").getAsString();
                                String credit = item.get("credit").getAsString();
                                observableList.add(new PurchaseOrderDTO(id, date, ledger, voucherType, voucherNo, debit, credit));
                            }


                            tblPurOrderDate.setCellValueFactory(new PropertyValueFactory<>("date"));
                            tblPurOrderLedger.setCellValueFactory(new PropertyValueFactory<>("ledger"));
                            tblPurOrderVoucherType.setCellValueFactory(new PropertyValueFactory<>("voucherType"));
                            tblPurOrderVoucherNo.setCellValueFactory(new PropertyValueFactory<>("voucherNo"));
                            tblPurOrderDebitAmt.setCellValueFactory(new PropertyValueFactory<>("debit"));
                            tblPurOrderCreditAmt.setCellValueFactory(new PropertyValueFactory<>("credit"));
                            tblPurOrderList.setItems(observableList);
                            originalData = observableList;
                            calculateTotalAmount();
                            duration = "";
                        } else {
                            purchaseOrderLogger.debug("Get getPurchaseOrderData responseObject is null...");
                            tblPurOrderList.getItems().clear();
                            duration = "";
                        }
                    }
                }
            });

            apiClient.setOnFailed(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    purchaseOrderLogger.error("Network API cancelled in getPurchaseOrderData()" + workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.setOnCancelled(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    purchaseOrderLogger.error("Network API cancelled in getPurchaseOrderData()" + workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.start();
            purchaseOrderLogger.debug("Get getPurchaseOrderData End...");

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            purchaseOrderLogger.error("Exception in getPurchaseOrderData():" + exceptionAsString);
        } finally {
            apiClient = null;
        }
    }

    private void handleDurationComboBoxAction(ActionEvent actionEvent) {
        selectedDuration = cmbPurOrderDuration.getSelectionModel().getSelectedItem();
        if (selectedDuration == "This Month") {
            duration = "month";
        } else if (selectedDuration == "Last Month") {
            duration = "lastMonth";
        } else if (selectedDuration == "Half Year") {
            duration = "halfYear";
        } else if (selectedDuration == "Full Year") {
            duration = "year";
            GlobalController.getInstance().addTabStatic(PURCHASE_ORDER_YEARLY_SLUG, false);
        }
        getPurchaseOrderData();

    }

    private void handleFilterComboBoxAction(ActionEvent actionEvent) {
        selectedFilter = cmbPurOrderSuplInv.getSelectionModel().getSelectedItem();
        if (cmbPurOrderSuplInv.getSelectionModel().getSelectedItem() == "Supplier") {
            selectedFilter = "supplier";
        } else if (cmbPurOrderSuplInv.getSelectionModel().getSelectedItem() == "Voucher No") {
            selectedFilter = "voucher";
        }
        getPurOrderDataByFilter();
    }

    private void getPurOrderDataByFilter() {
        ObservableList<PurchaseOrderDTO> filteredData = FXCollections.observableArrayList();

        if (selectedFilter.equalsIgnoreCase("supplier")) {
            tfPurOrderListSearch.textProperty().addListener((observable, oldValue, newValue) -> {
                filterData(newValue.trim(), PurchaseOrderDTO::getLedger);
            });
            calculateTotalAmount();
        } else if (selectedFilter.equalsIgnoreCase("voucher")) {
            tfPurOrderListSearch.textProperty().addListener((observable, oldValue, newValue) -> {
                filterData(newValue.trim(), PurchaseOrderDTO::getVoucherNo);
            });
            calculateTotalAmount();
        }
    }

    private void filterData(String keyword, Function<PurchaseOrderDTO, String> fieldExtractor) {
        ObservableList<PurchaseOrderDTO> filteredData = FXCollections.observableArrayList();
        purchaseOrderLogger.error("Search Purchase Order List in PurchaseOrderController");
        if (keyword.isEmpty()) {
            tblPurOrderList.setItems(originalData);
            calculateTotalAmount();
            return;
        }
        for (PurchaseOrderDTO item : originalData) {
            if (matchesKeyword(item, keyword, fieldExtractor)) {
                filteredData.add(item);
            }
        }
        tblPurOrderList.setItems(filteredData);
        calculateTotalAmount();
    }

    private boolean matchesKeyword(PurchaseOrderDTO item, String keyword, Function<PurchaseOrderDTO, String> fieldExtractor) {
        String lowerCaseKeyword = keyword.toLowerCase();
        String fieldValue = fieldExtractor.apply(item).toLowerCase();
        return fieldValue.contains(lowerCaseKeyword);
    }

    private void calculateTotalAmount() {
        double totalCredit = 0.0;
        double totalDebit = 0.0;
        for (PurchaseOrderDTO credit : tblPurOrderList.getItems()) {
//            System.out.println("credit AMounts : >>" + credit.getCredit());
            Double creditAmt = Double.parseDouble(credit.getCredit());
            totalCredit += creditAmt;
            Double debitAmt = Double.parseDouble(credit.getDebit());
            totalDebit += debitAmt;
        }
//        System.out.println("TotalAMount >> : " + totalCredit);
//        System.out.println("TotalAMount >> : " + totalDebit);

        lblTotalCredit.setText(String.valueOf(totalCredit));
        lblTotalDebit.setText(String.valueOf(totalDebit));
//        return totalCredit;
    }

    private void resetFields() {
//        body.clear();
        tfPurOrderListSearch.setText("");
        tfFromAmt.setText("");
        tfToAmt.setText("");
//        body.put("duration",duration);
        getPurchaseOrderData();
    }
}
