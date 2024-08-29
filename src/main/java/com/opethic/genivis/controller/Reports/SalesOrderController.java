package com.opethic.genivis.controller.Reports;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.opethic.genivis.dto.Reports.SalesOrderDTO;
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
import javafx.event.Event;
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

import static com.opethic.genivis.utils.FxmFileConstants.SALES_ORDER_YEARLY_SLUG;

public class SalesOrderController implements Initializable {
    @FXML
    private BorderPane bpSalesOrderRootPane;
    @FXML
    private ComboBox<String> cmbSalesOrderSuplInv, cmbSalesOrderDuration;
    @FXML
    private TextField tfSalesOrderListSearch, tfSalesOrderListFromDate, tfSalesOrderListToDate, tfFromAmt, tfToAmt;
    @FXML
    private Button btReset, btExportPdf, btExportExcel, btExportCsv, btExportPrint;
    @FXML
    private TableView<SalesOrderDTO> tblSalesOrderList;
    @FXML
    private TableColumn<SalesOrderDTO, String> tblSalesOrderDate, tblSalesOrderLedger, tblSalesOrderVoucherType, tblSalesOrderVoucherNo, tblSalesOrderDebitAmt, tblSalesOrderCreditAmt;
    @FXML
    private Label lblTotalDebit, lblTotalCredit;

    String duration = "";
    String selectedDuration = "", selectedFilter = "", selectedType = "";

    private ObservableList<SalesOrderDTO> originalData;

    String message = "";
    private JsonObject jsonObject = null;

    public static final Logger salesOrderLogger = LoggerFactory.getLogger(SalesOrderController.class);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> {
            tfSalesOrderListSearch.requestFocus();
        });

        // Initial Static Values Set Supplier/Voucher  Combobox
        cmbSalesOrderSuplInv.getItems().addAll("Supplier", "Voucher");
        cmbSalesOrderSuplInv.setPromptText("Filter Search");
        cmbSalesOrderSuplInv.setOnAction(this::handleFilterComboBoxAction);
        cmbSalesOrderSuplInv.setValue("Supplier");
        handleFilterComboBoxAction(null);
        //todo:open Filter dropdown on Space
        cmbSalesOrderSuplInv.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.SPACE && !cmbSalesOrderSuplInv.isShowing()) {
                cmbSalesOrderSuplInv.show();
                event.consume();
            }
        });

        // Initial Static Values Set Duration  Combobox
        cmbSalesOrderDuration.getItems().addAll("This Month", "Last Month", "Half Year", "Full Year");
        cmbSalesOrderDuration.setPromptText("Select Duration");
        cmbSalesOrderDuration.setOnAction(this::handleDurationComboBoxAction);
        cmbSalesOrderDuration.setValue(cmbSalesOrderDuration.getItems().get(0));
        duration = "month";
        //todo:open Filter dropdown on Space
        cmbSalesOrderDuration.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.SPACE && !cmbSalesOrderDuration.isShowing()) {
                cmbSalesOrderDuration.show();
                event.consume();
            }
        });


        getSalesOrderData();

        // Amountwise Filter after changing amount in the ToAMount field
        tfToAmt.textProperty().addListener((observable, oldValue, newValue) -> {
            filterAmountData();
        });

        btReset.setOnAction(e -> {
            resetFields();
        });

        // TextFields Converting in DateField
        DateValidator.applyDateFormat(tfSalesOrderListFromDate);
        DateValidator.applyDateFormat(tfSalesOrderListToDate);

        // Setting the handler for each node
        setKeyPressedHandler(cmbSalesOrderSuplInv);
        setKeyPressedHandler(tfSalesOrderListSearch);
        setKeyPressedHandler(cmbSalesOrderDuration);
        setKeyPressedHandler(tfSalesOrderListFromDate);
        setKeyPressedHandler(tfSalesOrderListToDate);
        setKeyPressedHandler(tfFromAmt);
        setKeyPressedHandler(tfToAmt);
        setKeyPressedHandler(btReset);
        setKeyPressedHandler(btExportPdf);
        setKeyPressedHandler(btExportExcel);
        setKeyPressedHandler(btExportCsv);
        setKeyPressedHandler(btExportPrint);
        tblSalesOrderList.requestFocus();

        tfSalesOrderListToDate.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                System.out.println("DateFilter From Date " + tfSalesOrderListFromDate.getText().toString());
                System.out.println("DateFilter To Date " + tfSalesOrderListToDate.getText().toString());
                if (!tfSalesOrderListFromDate.getText().equalsIgnoreCase("") && !tfSalesOrderListToDate.getText().equalsIgnoreCase("")) {
                    getSalesOrderData();
                }
                Node nextNode = CommonTraversa.getNextFocusableNode(tfFromAmt.getScene());
                if (nextNode != null) {
                    nextNode.requestFocus();
                }
            }
            if (event.getCode() == KeyCode.TAB) {
                if (!tfSalesOrderListFromDate.getText().equalsIgnoreCase("") && !tfSalesOrderListToDate.getText().equalsIgnoreCase("")) {
                    getSalesOrderData();
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
        salesOrderLogger.debug("Get filterAmountData Started...");
        String fromAmountText = tfFromAmt.getText();
        String toAmountText = tfToAmt.getText();
        if (toAmountText.isEmpty()) {
            tblSalesOrderList.setItems(FXCollections.observableList(originalData));
            calculateTotalAmount();
            return;
        }

        double fromAmount;
        double toAmount;
        String message = "Please Enter a Valid Amount Range.";
        String message1 = "Please Enter Valid Numeric Amounts.";
        Stage stage2 = (Stage) bpSalesOrderRootPane.getScene().getWindow();
        AlertUtility.CustomCallback callback = (number) -> {
            if (number == 1) {
            }
        };
        try {
            fromAmount = Double.parseDouble(fromAmountText);
            toAmount = Double.parseDouble(toAmountText);
        } catch (NumberFormatException e) {
            salesOrderLogger.error("Get filterAmountData NumberFormatException...");
            AlertUtility.AlertError(stage2, AlertUtility.alertTypeError, message1, callback);
            return;
        }


        List<SalesOrderDTO> filteredData = new ArrayList<>();
        for (SalesOrderDTO salesOrderData : originalData) {
            Double dobCred = Double.parseDouble(salesOrderData.getCredit());
            Double dobDeb = Double.parseDouble(salesOrderData.getDebit());
            if ((dobCred >= fromAmount && dobCred <= toAmount) || (dobDeb >= fromAmount && dobDeb <= toAmount)) {
                salesOrderLogger.debug("Get filterAmountData SuccessFully Executed...");
                filteredData.add(salesOrderData);
//                tblPurOrderList.setItems(purOrderData);
            }

        }

        tblSalesOrderList.setItems(FXCollections.observableList(filteredData));
        calculateTotalAmount();
        salesOrderLogger.debug("Get filterAmountData End...");
    }

    private void resetFields() {
        //        body.clear();
        tfSalesOrderListSearch.setText("");
        tfFromAmt.setText("");
        tfToAmt.setText("");
//        body.put("duration",duration);
        getSalesOrderData();
    }

    private void handleFilterComboBoxAction(ActionEvent event) {
        selectedFilter = cmbSalesOrderSuplInv.getSelectionModel().getSelectedItem();
        if (cmbSalesOrderSuplInv.getSelectionModel().getSelectedItem() == "Supplier") {
            selectedFilter = "supplier";
        } else if (cmbSalesOrderSuplInv.getSelectionModel().getSelectedItem() == "Voucher No") {
            selectedFilter = "voucher";
        }
        getSalesOrderDataByFilter();
    }


    private void handleDurationComboBoxAction(Event event) {
        selectedDuration = cmbSalesOrderDuration.getSelectionModel().getSelectedItem();
        if (selectedDuration == "This Month") {
            duration = "month";
        } else if (selectedDuration == "Last Month") {
            duration = "lastMonth";
        } else if (selectedDuration == "Half Year") {
            duration = "halfYear";
        } else if (selectedDuration == "Full Year") {
            duration = "year";
            GlobalController.getInstance().addTabStatic(SALES_ORDER_YEARLY_SLUG, false);
        }
        getSalesOrderData();
    }

    private void getSalesOrderDataByFilter() {
        ObservableList<SalesOrderDTO> filteredData = FXCollections.observableArrayList();

        if (selectedFilter.equalsIgnoreCase("supplier")) {
            tfSalesOrderListSearch.textProperty().addListener((observable, oldValue, newValue) -> {
                filterData(newValue.trim(), SalesOrderDTO::getLedger);
            });
            calculateTotalAmount();
        } else if (selectedFilter.equalsIgnoreCase("voucher")) {
            tfSalesOrderListSearch.textProperty().addListener((observable, oldValue, newValue) -> {
                filterData(newValue.trim(), SalesOrderDTO::getVoucherNo);
            });
            calculateTotalAmount();
        }
    }

    private void filterData(String keyword, Function<SalesOrderDTO, String> fieldExtractor) {
        ObservableList<SalesOrderDTO> filteredData = FXCollections.observableArrayList();
        salesOrderLogger.error("Search Sales Order List in SalesOrderController");
        if (keyword.isEmpty()) {
            tblSalesOrderList.setItems(originalData);
            calculateTotalAmount();
            return;
        }
        for (SalesOrderDTO item : originalData) {
            if (matchesKeyword(item, keyword, fieldExtractor)) {
                filteredData.add(item);
            }
        }
        tblSalesOrderList.setItems(filteredData);
        calculateTotalAmount();
    }

    private boolean matchesKeyword(SalesOrderDTO item, String keyword, Function<SalesOrderDTO, String> fieldExtractor) {
        String lowerCaseKeyword = keyword.toLowerCase();
        String fieldValue = fieldExtractor.apply(item).toLowerCase();
        return fieldValue.contains(lowerCaseKeyword);
    }

    private void calculateTotalAmount() {
        double totalCredit = 0.0;
        double totalDebit = 0.0;
        for (SalesOrderDTO credit : tblSalesOrderList.getItems()) {
            Double creditAmt = Double.parseDouble(credit.getCredit());
            totalCredit += creditAmt;
            Double debitAmt = Double.parseDouble(credit.getDebit());
            totalDebit += debitAmt;
        }

        lblTotalCredit.setText(String.valueOf(totalCredit));
        lblTotalDebit.setText(String.valueOf(totalDebit));
    }

    private void getSalesOrderData() {
        APIClient apiClient = null;
        salesOrderLogger.debug("Get getSalesOrderData Started...");
        Map<String, String> body = new HashMap<>();
        try {
//            System.out.println("selectedDuration --->>> " + duration);
            if(!duration.isEmpty()){
                body.put("duration", duration);
            }

            else if (!tfSalesOrderListFromDate.getText().equalsIgnoreCase("") && !tfSalesOrderListToDate.getText().equalsIgnoreCase("")) {
                System.out.println("date sdf ");
                body.put("start_date", Communicator.text_to_date.fromString(tfSalesOrderListFromDate.getText()).toString());
                body.put("end_date", Communicator.text_to_date.fromString(tfSalesOrderListToDate.getText()).toString());
            }
//            System.out.println("body --->>> " + body);
            String formData = Globals.mapToStringforFormData(body);
            apiClient = new APIClient(EndPoints.getSalesOrderData, formData, RequestType.FORM_DATA);
            apiClient.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    jsonObject = new Gson().fromJson(workerStateEvent.getSource().getValue().toString(), JsonObject.class);
//                    System.out.println("response =======> " + jsonObject);
                    ObservableList<SalesOrderDTO> observableList = FXCollections.observableArrayList();
                    if (jsonObject.get("responseStatus").getAsInt() == 200) {
                        salesOrderLogger.debug("Get getSalesOrderData Success...");
                        JsonArray responseArray = jsonObject.get("debit").getAsJsonArray();
//                        System.out.println("responseArray--------> " + responseArray);
                        String st_dt = (jsonObject.get("d_start_date").getAsString());
                        LocalDate start_date = LocalDate.parse(st_dt);
                        String en_dt = jsonObject.get("d_end_date").getAsString();
                        LocalDate end_date = LocalDate.parse(en_dt);

                        tfSalesOrderListFromDate.setText(start_date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                        tfSalesOrderListToDate.setText(end_date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

                        if (responseArray.size() > 0){
                            for (JsonElement element : responseArray) {
                                JsonObject item = element.getAsJsonObject();
                                String id = item.get("row_id").getAsString();
                                String date = item.get("transaction_date").getAsString();
                                String ledger = item.get("particulars").getAsString();
                                String voucherType = item.get("voucher_type").getAsString();
                                String voucherNo = item.get("voucher_no").getAsString();
                                String debit = item.get("debit").getAsString();
                                String credit = item.get("credit").getAsString();
                                observableList.add(new SalesOrderDTO(id, date, ledger, voucherType, voucherNo, debit, credit));
                            }

                            tblSalesOrderDate.setCellValueFactory(new PropertyValueFactory<>("date"));
                            tblSalesOrderLedger.setCellValueFactory(new PropertyValueFactory<>("ledger"));
                            tblSalesOrderVoucherType.setCellValueFactory(new PropertyValueFactory<>("voucherType"));
                            tblSalesOrderVoucherNo.setCellValueFactory(new PropertyValueFactory<>("voucherNo"));
                            tblSalesOrderDebitAmt.setCellValueFactory(new PropertyValueFactory<>("debit"));
                            tblSalesOrderCreditAmt.setCellValueFactory(new PropertyValueFactory<>("credit"));
                            tblSalesOrderList.setItems(observableList);
                            originalData = observableList;
                            calculateTotalAmount();
                            duration="";
                        } else {
                            salesOrderLogger.debug("Get getSalesOrderData responseObject is null...");
                            tblSalesOrderList.getItems().clear();
                            duration="";
                        }
                    }
                }
            });

            apiClient.setOnFailed(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    salesOrderLogger.error("Network API cancelled in getSalesOrderData()" + workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.setOnCancelled(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    salesOrderLogger.error("Network API cancelled in getSalesOrderData()" + workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.start();
            salesOrderLogger.debug("Get getSalesOrderData End...");

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            salesOrderLogger.error("Exception in getSalesOrderData():" + exceptionAsString);
        } finally {
            apiClient = null;
        }
    }

}
