package com.opethic.genivis.controller.Reports;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.opethic.genivis.dto.Reports.SalesChallanDTO;
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

import static com.opethic.genivis.utils.FxmFileConstants.SALES_CHALLAN_YEARLY_CONTROLLER;
import static com.opethic.genivis.utils.FxmFileConstants.SALES_CHALLAN_YEARLY_SLUG;

public class SalesChallanController implements Initializable {
    @FXML
    private BorderPane bpSalesChallanRootPane;
    @FXML
    private ComboBox<String> cmbSalesChallanSuplInv, cmbSalesChallanDuration;
    @FXML
    private TextField tfSalesChallanListSearch, tfSalesChallanListFromDate, tfSalesChallanListToDate, tfFromAmt, tfToAmt;
    @FXML
    private Button btReset, btExportPdf, btExportExcel, btExportCsv, btExportPrint;
    @FXML
    private TableView<SalesChallanDTO> tblSalesChallanList;
    @FXML
    private TableColumn<SalesChallanDTO, String> tblSalesChallanDate, tblSalesChallanLedger, tblSalesChallanVoucherType, tblSalesChallanVoucherNo, tblSalesChallanDebitAmt, tblSalesChallanCreditAmt;
    @FXML
    private Label lblTotalDebit, lblTotalCredit;

    String duration = "";
    String selectedDuration = "", selectedFilter = "", selectedType = "";

    private ObservableList<SalesChallanDTO> originalData;

    String message = "";
    private JsonObject jsonObject = null;

    public static final Logger salesChallanLogger = LoggerFactory.getLogger(SalesChallanController.class);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> {
            tfSalesChallanListSearch.requestFocus();
        });

        // Initial Static Values Set Supplier/Voucher  Combobox
        cmbSalesChallanSuplInv.getItems().addAll("Supplier", "Voucher");
        cmbSalesChallanSuplInv.setPromptText("Filter Search");
        cmbSalesChallanSuplInv.setOnAction(this::handleFilterComboBoxAction);
        cmbSalesChallanSuplInv.setValue("Supplier");
        handleFilterComboBoxAction(null);
        //todo:open Filter dropdown on Space
        cmbSalesChallanSuplInv.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.SPACE && !cmbSalesChallanSuplInv.isShowing()) {
                cmbSalesChallanSuplInv.show();
                event.consume();
            }
        });

        // Initial Static Values Set Duration  Combobox
        cmbSalesChallanDuration.getItems().addAll("This Month", "Last Month", "Half Year", "Full Year");
        cmbSalesChallanDuration.setPromptText("Select Duration");
        cmbSalesChallanDuration.setOnAction(this::handleDurationComboBoxAction);
        cmbSalesChallanDuration.setValue(cmbSalesChallanDuration.getItems().get(0));
        duration = "month";
        //todo:open Filter dropdown on Space
        cmbSalesChallanDuration.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.SPACE && !cmbSalesChallanDuration.isShowing()) {
                cmbSalesChallanDuration.show();
                event.consume();
            }
        });


        getSalesChallanData();

        // Amountwise Filter after changing amount in the ToAMount field
        tfToAmt.textProperty().addListener((observable, oldValue, newValue) -> {
            filterAmountData();
        });

        btReset.setOnAction(e -> {
            resetFields();
        });

        // TextFields Converting in DateField
        DateValidator.applyDateFormat(tfSalesChallanListFromDate);
        DateValidator.applyDateFormat(tfSalesChallanListToDate);

        // Setting the handler for each node
        setKeyPressedHandler(cmbSalesChallanSuplInv);
        setKeyPressedHandler(tfSalesChallanListSearch);
        setKeyPressedHandler(cmbSalesChallanDuration);
        setKeyPressedHandler(tfSalesChallanListFromDate);
        setKeyPressedHandler(tfSalesChallanListToDate);
        setKeyPressedHandler(tfFromAmt);
        setKeyPressedHandler(tfToAmt);
        setKeyPressedHandler(btReset);
        setKeyPressedHandler(btExportPdf);
        setKeyPressedHandler(btExportExcel);
        setKeyPressedHandler(btExportCsv);
        setKeyPressedHandler(btExportPrint);
        tblSalesChallanList.requestFocus();

        tfSalesChallanListToDate.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                System.out.println("DateFilter From Date " + tfSalesChallanListFromDate.getText().toString());
                System.out.println("DateFilter To Date " + tfSalesChallanListToDate.getText().toString());
                if (!tfSalesChallanListFromDate.getText().equalsIgnoreCase("") && !tfSalesChallanListToDate.getText().equalsIgnoreCase("")) {
                    getSalesChallanData();
                }
                Node nextNode = CommonTraversa.getNextFocusableNode(tfFromAmt.getScene());
                if (nextNode != null) {
                    nextNode.requestFocus();
                }
            }
            if (event.getCode() == KeyCode.TAB) {
                if (!tfSalesChallanListFromDate.getText().equalsIgnoreCase("") && !tfSalesChallanListToDate.getText().equalsIgnoreCase("")) {
                    getSalesChallanData();
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
        salesChallanLogger.debug("Get filterAmountData Started...");
        String fromAmountText = tfFromAmt.getText();
        String toAmountText = tfToAmt.getText();
        if (toAmountText.isEmpty()) {
            tblSalesChallanList.setItems(FXCollections.observableList(originalData));
            calculateTotalAmount();
            return;
        }

        double fromAmount;
        double toAmount;
        String message = "Please Enter a Valid Amount Range.";
        String message1 = "Please Enter Valid Numeric Amounts.";
        Stage stage2 = (Stage) bpSalesChallanRootPane.getScene().getWindow();
        AlertUtility.CustomCallback callback = (number) -> {
            if (number == 1) {
            }
        };
        try {
            fromAmount = Double.parseDouble(fromAmountText);
            toAmount = Double.parseDouble(toAmountText);
        } catch (NumberFormatException e) {
            salesChallanLogger.error("Get filterAmountData NumberFormatException...");
            AlertUtility.AlertError(stage2, AlertUtility.alertTypeError, message1, callback);
            return;
        }


        List<SalesChallanDTO> filteredData = new ArrayList<>();
        for (SalesChallanDTO salesChallanData : originalData) {
            Double dobCred = Double.parseDouble(salesChallanData.getCredit());
            Double dobDeb = Double.parseDouble(salesChallanData.getDebit());
            if ((dobCred >= fromAmount && dobCred <= toAmount) || (dobDeb >= fromAmount && dobDeb <= toAmount)) {
                salesChallanLogger.debug("Get filterAmountData SuccessFully Executed...");
                filteredData.add(salesChallanData);
//                tblPurChallanList.setItems(purChallanData);
            }

        }

        tblSalesChallanList.setItems(FXCollections.observableList(filteredData));
        calculateTotalAmount();
        salesChallanLogger.debug("Get filterAmountData End...");
    }

    private void resetFields() {
        //        body.clear();
        tfSalesChallanListSearch.setText("");
        tfFromAmt.setText("");
        tfToAmt.setText("");
//        body.put("duration",duration);
        getSalesChallanData();
    }

    private void handleFilterComboBoxAction(ActionEvent event) {
        selectedFilter = cmbSalesChallanSuplInv.getSelectionModel().getSelectedItem();
        if (cmbSalesChallanSuplInv.getSelectionModel().getSelectedItem() == "Supplier") {
            selectedFilter = "supplier";
        } else if (cmbSalesChallanSuplInv.getSelectionModel().getSelectedItem() == "Voucher No") {
            selectedFilter = "voucher";
        }
        getSalesChallanDataByFilter();
    }


    private void handleDurationComboBoxAction(Event event) {
        selectedDuration = cmbSalesChallanDuration.getSelectionModel().getSelectedItem();
        if (selectedDuration == "This Month") {
            duration = "month";
        } else if (selectedDuration == "Last Month") {
            duration = "lastMonth";
        } else if (selectedDuration == "Half Year") {
            duration = "halfYear";
        } else if (selectedDuration == "Full Year") {
            duration = "year";
            GlobalController.getInstance().addTabStatic(SALES_CHALLAN_YEARLY_SLUG, false);
        }
        getSalesChallanData();
    }

    private void getSalesChallanDataByFilter() {
        ObservableList<SalesChallanDTO> filteredData = FXCollections.observableArrayList();

        if (selectedFilter.equalsIgnoreCase("supplier")) {
            tfSalesChallanListSearch.textProperty().addListener((observable, oldValue, newValue) -> {
                filterData(newValue.trim(), SalesChallanDTO::getLedger);
            });
            calculateTotalAmount();
        } else if (selectedFilter.equalsIgnoreCase("voucher")) {
            tfSalesChallanListSearch.textProperty().addListener((observable, oldValue, newValue) -> {
                filterData(newValue.trim(), SalesChallanDTO::getVoucherNo);
            });
            calculateTotalAmount();
        }
    }

    private void filterData(String keyword, Function<SalesChallanDTO, String> fieldExtractor) {
        ObservableList<SalesChallanDTO> filteredData = FXCollections.observableArrayList();
        salesChallanLogger.error("Search Sales Challan List in SalesChallanController");
        if (keyword.isEmpty()) {
            tblSalesChallanList.setItems(originalData);
            calculateTotalAmount();
            return;
        }
        for (SalesChallanDTO item : originalData) {
            if (matchesKeyword(item, keyword, fieldExtractor)) {
                filteredData.add(item);
            }
        }
        tblSalesChallanList.setItems(filteredData);
        calculateTotalAmount();
    }

    private boolean matchesKeyword(SalesChallanDTO item, String keyword, Function<SalesChallanDTO, String> fieldExtractor) {
        String lowerCaseKeyword = keyword.toLowerCase();
        String fieldValue = fieldExtractor.apply(item).toLowerCase();
        return fieldValue.contains(lowerCaseKeyword);
    }

    private void calculateTotalAmount() {
        double totalCredit = 0.0;
        double totalDebit = 0.0;
        for (SalesChallanDTO credit : tblSalesChallanList.getItems()) {
            Double creditAmt = Double.parseDouble(credit.getCredit());
            totalCredit += creditAmt;
            Double debitAmt = Double.parseDouble(credit.getDebit());
            totalDebit += debitAmt;
        }

        lblTotalCredit.setText(String.valueOf(totalCredit));
        lblTotalDebit.setText(String.valueOf(totalDebit));
    }

    private void getSalesChallanData() {
        APIClient apiClient = null;
        salesChallanLogger.debug("Get getSalesChallanData Started...");
        Map<String, String> body = new HashMap<>();
        try {
//            System.out.println("selectedDuration --->>> " + duration);
            if(!duration.isEmpty()){
                body.put("duration", duration);
            }

            else if (!tfSalesChallanListFromDate.getText().equalsIgnoreCase("") && !tfSalesChallanListToDate.getText().equalsIgnoreCase("")) {
                System.out.println("date sdf ");
                body.put("start_date", Communicator.text_to_date.fromString(tfSalesChallanListFromDate.getText()).toString());
                body.put("end_date", Communicator.text_to_date.fromString(tfSalesChallanListToDate.getText()).toString());
            }
//            System.out.println("body --->>> " + body);
            String formData = Globals.mapToStringforFormData(body);
            apiClient = new APIClient(EndPoints.getSalesChallanData, formData, RequestType.FORM_DATA);
            apiClient.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    jsonObject = new Gson().fromJson(workerStateEvent.getSource().getValue().toString(), JsonObject.class);
//                    System.out.println("response =======> " + jsonObject);
                    ObservableList<SalesChallanDTO> observableList = FXCollections.observableArrayList();
                    if (jsonObject.get("responseStatus").getAsInt() == 200) {
                        salesChallanLogger.debug("Get getSalesChallanData Success...");
                        JsonArray responseArray = jsonObject.get("debit").getAsJsonArray();
//                        System.out.println("responseArray--------> " + responseArray);
                        String st_dt = (jsonObject.get("d_start_date").getAsString());
                        LocalDate start_date = LocalDate.parse(st_dt);
                        String en_dt = jsonObject.get("d_end_date").getAsString();
                        LocalDate end_date = LocalDate.parse(en_dt);

                        tfSalesChallanListFromDate.setText(start_date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                        tfSalesChallanListToDate.setText(end_date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

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
                                observableList.add(new SalesChallanDTO(id, date, ledger, voucherType, voucherNo, debit, credit));
                            }

                            tblSalesChallanDate.setCellValueFactory(new PropertyValueFactory<>("date"));
                            tblSalesChallanLedger.setCellValueFactory(new PropertyValueFactory<>("ledger"));
                            tblSalesChallanVoucherType.setCellValueFactory(new PropertyValueFactory<>("voucherType"));
                            tblSalesChallanVoucherNo.setCellValueFactory(new PropertyValueFactory<>("voucherNo"));
                            tblSalesChallanDebitAmt.setCellValueFactory(new PropertyValueFactory<>("debit"));
                            tblSalesChallanCreditAmt.setCellValueFactory(new PropertyValueFactory<>("credit"));
                            tblSalesChallanList.setItems(observableList);
                            originalData = observableList;
                            calculateTotalAmount();
                            duration="";
                        } else {
                            salesChallanLogger.debug("Get getSalesChallanData responseObject is null...");
                            tblSalesChallanList.getItems().clear();
                            duration="";
                        }
                    }
                }
            });

            apiClient.setOnFailed(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    salesChallanLogger.error("Network API cancelled in getSalesChallanData()" + workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.setOnCancelled(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    salesChallanLogger.error("Network API cancelled in getSalesChallanData()" + workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.start();
            salesChallanLogger.debug("Get getSalesChallanData End...");

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            salesChallanLogger.error("Exception in getSalesChallanData():" + exceptionAsString);
        } finally {
            apiClient = null;
        }
    }
}
