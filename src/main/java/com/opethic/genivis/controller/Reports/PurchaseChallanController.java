package com.opethic.genivis.controller.Reports;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.opethic.genivis.dto.Reports.PurchaseChallanDTO;
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

import static com.opethic.genivis.utils.FxmFileConstants.PURCHASE_CHALLAN_YEARLY_SLUG;

public class PurchaseChallanController implements Initializable {
    @FXML
    private BorderPane bpPurChallanRootPane;
    @FXML
    private ComboBox<String> cmbPurChallanSuplInv, cmbPurChallanDuration;
    @FXML
    private TextField tfPurChallanListSearch, tfPurChallanListFromDate, tfPurChallanListToDate, tfFromAmt, tfToAmt;
    @FXML
    private Button btReset, btExportPdf, btExportExcel, btExportCsv, btExportPrint;
    @FXML
    private TableView<PurchaseChallanDTO> tblPurChallanList;
    @FXML
    private TableColumn<PurchaseChallanDTO, String> tblPurChallanDate, tblPurChallanLedger, tblPurChallanVoucherType, tblPurChallanVoucherNo, tblPurChallanDebitAmt, tblPurChallanCreditAmt;
    @FXML
    private Label lblTotalDebit, lblTotalCredit;

    String duration = "";
    String selectedDuration = "", selectedFilter = "", selectedType = "";

    private ObservableList<PurchaseChallanDTO> originalData;

    String message = "";
    private JsonObject jsonObject = null;

    public static final Logger purchaseChallanLogger = LoggerFactory.getLogger(PurchaseChallanController.class);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> {
            tfPurChallanListSearch.requestFocus();
        });

        // Initial Static Values Set Supplier/Voucher  Combobox
        cmbPurChallanSuplInv.getItems().addAll("Supplier", "Voucher");
        cmbPurChallanSuplInv.setPromptText("Filter Search");
        cmbPurChallanSuplInv.setOnAction(this::handleFilterComboBoxAction);
        cmbPurChallanSuplInv.setValue("Supplier");
        handleFilterComboBoxAction(null);
        //todo:open Filter dropdown on Space
        cmbPurChallanSuplInv.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.SPACE && !cmbPurChallanSuplInv.isShowing()) {
                cmbPurChallanSuplInv.show();
                event.consume();
            }
        });

        // Initial Static Values Set Duration  Combobox
        cmbPurChallanDuration.getItems().addAll("This Month", "Last Month", "Half Year", "Full Year");
        cmbPurChallanDuration.setPromptText("Select Duration");
        cmbPurChallanDuration.setOnAction(this::handleDurationComboBoxAction);
        cmbPurChallanDuration.setValue(cmbPurChallanDuration.getItems().get(0));
        duration = "month";
        //todo:open Filter dropdown on Space
        cmbPurChallanDuration.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.SPACE && !cmbPurChallanDuration.isShowing()) {
                cmbPurChallanDuration.show();
                event.consume();
            }
        });

        getPurchaseChallanData();

        // Amountwise Filter after changing amount in the ToAMount field
        tfToAmt.textProperty().addListener((observable, oldValue, newValue) -> {
            filterAmountData();
        });

        btReset.setOnAction(e -> {
            resetFields();
        });

        // TextFields Converting in DateField
        DateValidator.applyDateFormat(tfPurChallanListFromDate);
        DateValidator.applyDateFormat(tfPurChallanListToDate);

        // Setting the handler for each node
        setKeyPressedHandler(cmbPurChallanSuplInv);
        setKeyPressedHandler(tfPurChallanListSearch);
        setKeyPressedHandler(cmbPurChallanDuration);
        setKeyPressedHandler(tfPurChallanListFromDate);
        setKeyPressedHandler(tfPurChallanListToDate);
        setKeyPressedHandler(tfFromAmt);
        setKeyPressedHandler(tfToAmt);
        setKeyPressedHandler(btReset);
        setKeyPressedHandler(btExportPdf);
        setKeyPressedHandler(btExportExcel);
        setKeyPressedHandler(btExportCsv);
        setKeyPressedHandler(btExportPrint);
        tblPurChallanList.requestFocus();

        tfPurChallanListToDate.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                System.out.println("DateFilter From Date " + tfPurChallanListFromDate.getText().toString());
                System.out.println("DateFilter To Date " + tfPurChallanListToDate.getText().toString());
                if (!tfPurChallanListFromDate.getText().equalsIgnoreCase("") && !tfPurChallanListToDate.getText().equalsIgnoreCase("")) {
                    getPurchaseChallanData();
                }
                Node nextNode = CommonTraversa.getNextFocusableNode(tfFromAmt.getScene());
                if (nextNode != null) {
                    nextNode.requestFocus();
                }
            }
            if (event.getCode() == KeyCode.TAB) {
                if (!tfPurChallanListFromDate.getText().equalsIgnoreCase("") && !tfPurChallanListToDate.getText().equalsIgnoreCase("")) {
                    getPurchaseChallanData();
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

    private void handleFilterComboBoxAction(ActionEvent event) {
        selectedFilter = cmbPurChallanSuplInv.getSelectionModel().getSelectedItem();
        if (cmbPurChallanSuplInv.getSelectionModel().getSelectedItem() == "Supplier") {
            selectedFilter = "supplier";
        } else if (cmbPurChallanSuplInv.getSelectionModel().getSelectedItem() == "Voucher No") {
            selectedFilter = "voucher";
        }
        getPurChallanDataByFilter();
    }

    private void getPurChallanDataByFilter() {
        ObservableList<PurchaseChallanDTO> filteredData = FXCollections.observableArrayList();

        if (selectedFilter.equalsIgnoreCase("supplier")) {
            tfPurChallanListSearch.textProperty().addListener((observable, oldValue, newValue) -> {
                filterData(newValue.trim(), PurchaseChallanDTO::getLedger);
            });
            calculateTotalAmount();
        } else if (selectedFilter.equalsIgnoreCase("voucher")) {
            tfPurChallanListSearch.textProperty().addListener((observable, oldValue, newValue) -> {
                filterData(newValue.trim(), PurchaseChallanDTO::getVoucherNo);
            });
            calculateTotalAmount();
        }
    }

    private void filterData(String keyword, Function<PurchaseChallanDTO, String> fieldExtractor) {
        ObservableList<PurchaseChallanDTO> filteredData = FXCollections.observableArrayList();
        purchaseChallanLogger.error("Search Purchase Challan List in PurchaseChallanController");
        if (keyword.isEmpty()) {
            tblPurChallanList.setItems(originalData);
            calculateTotalAmount();
            return;
        }
        for (PurchaseChallanDTO item : originalData) {
            if (matchesKeyword(item, keyword, fieldExtractor)) {
                filteredData.add(item);
            }
        }
        tblPurChallanList.setItems(filteredData);
        calculateTotalAmount();
    }

    private boolean matchesKeyword(PurchaseChallanDTO item, String keyword, Function<PurchaseChallanDTO, String> fieldExtractor) {
        String lowerCaseKeyword = keyword.toLowerCase();
        String fieldValue = fieldExtractor.apply(item).toLowerCase();
        return fieldValue.contains(lowerCaseKeyword);
    }

    private void handleDurationComboBoxAction(Event event) {
        selectedDuration = cmbPurChallanDuration.getSelectionModel().getSelectedItem();
        if (selectedDuration == "This Month") {
            duration = "month";
        } else if (selectedDuration == "Last Month") {
            duration = "lastMonth";
        } else if (selectedDuration == "Half Year") {
            duration = "halfYear";
        } else if (selectedDuration == "Full Year") {
            duration = "year";
            GlobalController.getInstance().addTabStatic(PURCHASE_CHALLAN_YEARLY_SLUG, false);
        }
        getPurchaseChallanData();

    }

    private void getPurchaseChallanData() {
        APIClient apiClient = null;
        purchaseChallanLogger.debug("Get getPurchaseChallanData Started...");
        Map<String, String> body = new HashMap<>(); // this used for mapping the data to call getPurchaseChallanData() as per requirement  sending the Formdata
        try {
            System.out.println("selectedDuration --->>> " + duration);
            if(!duration.isEmpty()){
                body.put("duration", duration);
            }

            else if (!tfPurChallanListFromDate.getText().equalsIgnoreCase("") && !tfPurChallanListToDate.getText().equalsIgnoreCase("")) {
                System.out.println("date sdf ");
                body.put("start_date", Communicator.text_to_date.fromString(tfPurChallanListFromDate.getText()).toString());
                body.put("end_date", Communicator.text_to_date.fromString(tfPurChallanListToDate.getText()).toString());
            }
            System.out.println("body --->>> " + body);
            String formData = Globals.mapToStringforFormData(body);
            apiClient = new APIClient(EndPoints.getPurchaseChallanData, formData, RequestType.FORM_DATA);
            apiClient.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    jsonObject = new Gson().fromJson(workerStateEvent.getSource().getValue().toString(), JsonObject.class);
                    System.out.println("response =======> " + jsonObject);
                    ObservableList<PurchaseChallanDTO> observableList = FXCollections.observableArrayList();
                    if (jsonObject.get("responseStatus").getAsInt() == 200) {
                        purchaseChallanLogger.debug("Get getPurchaseChallanData Success...");
                        JsonArray responseArray = jsonObject.get("debit").getAsJsonArray();
//                        System.out.println("responseArray--------> " + responseArray);
                        String st_dt = (jsonObject.get("d_start_date").getAsString());
                        LocalDate start_date = LocalDate.parse(st_dt);
                        String en_dt = jsonObject.get("d_end_date").getAsString();
                        LocalDate end_date = LocalDate.parse(en_dt);

                        tfPurChallanListFromDate.setText(start_date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                        tfPurChallanListToDate.setText(end_date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

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
                                observableList.add(new PurchaseChallanDTO(id, date, ledger, voucherType, voucherNo, debit, credit));
                            }


                            tblPurChallanDate.setCellValueFactory(new PropertyValueFactory<>("date"));
                            tblPurChallanLedger.setCellValueFactory(new PropertyValueFactory<>("ledger"));
                            tblPurChallanVoucherType.setCellValueFactory(new PropertyValueFactory<>("voucherType"));
                            tblPurChallanVoucherNo.setCellValueFactory(new PropertyValueFactory<>("voucherNo"));
                            tblPurChallanDebitAmt.setCellValueFactory(new PropertyValueFactory<>("debit"));
                            tblPurChallanCreditAmt.setCellValueFactory(new PropertyValueFactory<>("credit"));
                            tblPurChallanList.setItems(observableList);
                            originalData = observableList;
                            calculateTotalAmount();
                            duration="";
                        } else {
                            purchaseChallanLogger.debug("Get getPurchaseChallanData responseObject is null...");
                            tblPurChallanList.getItems().clear();
                            duration="";
                        }
//                        setCreditNoteFormData(creditNoteByIdMainDTO);
                    }
                }
            });

            apiClient.setOnFailed(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    purchaseChallanLogger.error("Network API cancelled in getPurchaseChallanData()" + workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.setOnCancelled(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    purchaseChallanLogger.error("Network API cancelled in getPurchaseChallanData()" + workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.start();
            purchaseChallanLogger.debug("Get getPurchaseChallanData End...");

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            purchaseChallanLogger.error("Exception in getPurchaseChallanData():" + exceptionAsString);
        } finally {
            apiClient = null;
        }
    }

    private void filterAmountData() {
        purchaseChallanLogger.debug("Get filterAmountData Started...");
        String fromAmountText = tfFromAmt.getText();
        String toAmountText = tfToAmt.getText();
        if (toAmountText.isEmpty()) {
            tblPurChallanList.setItems(FXCollections.observableList(originalData));
            calculateTotalAmount();
            return;
        }

        double fromAmount;
        double toAmount;
        String message = "Please Enter a Valid Amount Range.";
        String message1 = "Please Enter Valid Numeric Amounts.";
        Stage stage2 = (Stage) bpPurChallanRootPane.getScene().getWindow();
        AlertUtility.CustomCallback callback = (number) -> {
            if (number == 1) {
            }
        };
        try {
            fromAmount = Double.parseDouble(fromAmountText);
            toAmount = Double.parseDouble(toAmountText);
        } catch (NumberFormatException e) {
            purchaseChallanLogger.error("Get filterAmountData NumberFormatException...");
            AlertUtility.AlertError(stage2, AlertUtility.alertTypeError, message1, callback);
            return;
        }


//        if (fromAmount > toAmount) {
//            tfToAmt.setOnKeyPressed(e->{
//                if(e.getCode() == KeyCode.ENTER){
//                    if(fromAmount > toAmount){
//                        purchaseChallanLogger.error("Get filterAmountData fromAmount > toAmount...");
//                        AlertUtility.AlertError(stage2, AlertUtility.alertTypeError, message, callback);
//                    }
//                }
//            });
//            return;
//        }

        List<PurchaseChallanDTO> filteredData = new ArrayList<>();
        for (PurchaseChallanDTO purChallanData : originalData) {
            Double dobCred = Double.parseDouble(purChallanData.getCredit());
            Double dobDeb = Double.parseDouble(purChallanData.getDebit());
            if ((dobCred >= fromAmount && dobCred <= toAmount) || (dobDeb >= fromAmount && dobDeb <= toAmount)) {
                purchaseChallanLogger.debug("Get filterAmountData SuccessFully Executed...");
                filteredData.add(purChallanData);
//                tblPurChallanList.setItems(purChallanData);
            }

        }

        tblPurChallanList.setItems(FXCollections.observableList(filteredData));
        calculateTotalAmount();
        purchaseChallanLogger.debug("Get filterAmountData End...");
    }

    private void calculateTotalAmount() {
        double totalCredit = 0.0;
        double totalDebit = 0.0;
        for (PurchaseChallanDTO credit : tblPurChallanList.getItems()) {
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
        tfPurChallanListSearch.setText("");
        tfFromAmt.setText("");
        tfToAmt.setText("");
//        body.put("duration",duration);
        getPurchaseChallanData();
    }

}
