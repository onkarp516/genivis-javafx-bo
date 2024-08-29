package com.opethic.genivis.controller.Reports;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.opethic.genivis.dto.Reports.PurchaseRegisterDTO;
import com.opethic.genivis.dto.Reports.SalesRegisterDTO;
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

import static com.opethic.genivis.utils.FxmFileConstants.PURCHASE_REGISTER_YEARLY_SLUG;
import static com.opethic.genivis.utils.FxmFileConstants.SALES_REGISTER_YEARLY_SLUG;

public class SalesRegisterController implements Initializable {

    @FXML
    private BorderPane bpSalesRegisterRootPane;
    @FXML
    private ComboBox<String> cmbSalesRegSuplInv, cmbSalesRegDuration;
    @FXML
    private TextField tfSalesRegListSearch, tfSalesRegListFromDate, tfSalesRegListToDate, tfFromAmt, tfToAmt;
    @FXML
    private Button btReset, btExportPdf, btExportExcel, btExportCsv, btExportPrint;
    @FXML
    private TableView<SalesRegisterDTO> tblSalesRegList;
    @FXML
    private TableColumn<SalesRegisterDTO, String> tblSalesRegDate, tblSalesRegLedger, tblSalesRegVoucherType, tblSalesRegVoucherNo, tblSalesRegDebitAmt, tblSalesRegCreditAmt;
    @FXML
    private Label lblTotalDebit, lblTotalCredit;

    String duration = "";
    String selectedDuration = "", selectedFilter = "", selectedType = "";

    private ObservableList<SalesRegisterDTO> originalData;

    String message = "";
    private JsonObject jsonObject = null;

    public static final Logger salesRegisterLogger = LoggerFactory.getLogger(SalesRegisterController.class);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> {
            tfSalesRegListSearch.requestFocus();
        });

        // Initial Static Values Set Supplier/Voucher  Combobox
        cmbSalesRegSuplInv.getItems().addAll("Supplier", "Voucher");
        cmbSalesRegSuplInv.setPromptText("Filter Search");
        cmbSalesRegSuplInv.setOnAction(this::handleFilterComboBoxAction);
        cmbSalesRegSuplInv.setValue("Supplier");
        handleFilterComboBoxAction(null);
        //todo:open Filter dropdown on Space
        cmbSalesRegSuplInv.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.SPACE && !cmbSalesRegSuplInv.isShowing()) {
                cmbSalesRegSuplInv.show();
                event.consume();
            }
        });

        // Initial Static Values Set Duration  Combobox
        cmbSalesRegDuration.getItems().addAll("This Month", "Last Month", "Half Year", "Full Year");
        cmbSalesRegDuration.setPromptText("Select Duration");
        cmbSalesRegDuration.setOnAction(this::handleDurationComboBoxAction);
        cmbSalesRegDuration.setValue(cmbSalesRegDuration.getItems().get(0));
        duration = "month";
        //todo:open Filter dropdown on Space
        cmbSalesRegDuration.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.SPACE && !cmbSalesRegDuration.isShowing()) {
                cmbSalesRegDuration.show();
                event.consume();
            }
        });


        getSalesRegisterData();

        // Amountwise Filter after changing amount in the ToAMount field
        tfToAmt.textProperty().addListener((observable, oldValue, newValue) -> {
            filterAmountData();
        });

        btReset.setOnAction(e -> {
            resetFields();
        });

        // TextFields Converting in DateField
        DateValidator.applyDateFormat(tfSalesRegListFromDate);
        DateValidator.applyDateFormat(tfSalesRegListToDate);

        // Setting the handler for each node
        setKeyPressedHandler(cmbSalesRegSuplInv);
        setKeyPressedHandler(tfSalesRegListSearch);
        setKeyPressedHandler(cmbSalesRegDuration);
        setKeyPressedHandler(tfSalesRegListFromDate);
        setKeyPressedHandler(tfSalesRegListToDate);
        setKeyPressedHandler(tfFromAmt);
        setKeyPressedHandler(tfToAmt);
        setKeyPressedHandler(btReset);
        setKeyPressedHandler(btExportPdf);
        setKeyPressedHandler(btExportExcel);
        setKeyPressedHandler(btExportCsv);
        setKeyPressedHandler(btExportPrint);
        tblSalesRegList.requestFocus();

        tfSalesRegListToDate.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                System.out.println("DateFilter From Date " + tfSalesRegListFromDate.getText().toString());
                System.out.println("DateFilter To Date " + tfSalesRegListToDate.getText().toString());
                if (!tfSalesRegListFromDate.getText().equalsIgnoreCase("") && !tfSalesRegListToDate.getText().equalsIgnoreCase("")) {
                    getSalesRegisterData();
                }
                Node nextNode = CommonTraversa.getNextFocusableNode(tfFromAmt.getScene());
                if (nextNode != null) {
                    nextNode.requestFocus();
                }
            }
            if (event.getCode() == KeyCode.TAB) {
                if (!tfSalesRegListFromDate.getText().equalsIgnoreCase("") && !tfSalesRegListToDate.getText().equalsIgnoreCase("")) {
                    getSalesRegisterData();
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
        salesRegisterLogger.debug("Get filterAmountData Started...");
        String fromAmountText = tfFromAmt.getText();
        String toAmountText = tfToAmt.getText();
        if (toAmountText.isEmpty()) {
            tblSalesRegList.setItems(FXCollections.observableList(originalData));
            calculateTotalAmount();
            return;
        }

        double fromAmount;
        double toAmount;
        String message = "Please Enter a Valid Amount Range.";
        String message1 = "Please Enter Valid Numeric Amounts.";
        Stage stage2 = (Stage) bpSalesRegisterRootPane.getScene().getWindow();
        AlertUtility.CustomCallback callback = (number) -> {
            if (number == 1) {
            }
        };
        try {
            fromAmount = Double.parseDouble(fromAmountText);
            toAmount = Double.parseDouble(toAmountText);
        } catch (NumberFormatException e) {
            salesRegisterLogger.error("Get filterAmountData NumberFormatException...");
            AlertUtility.AlertError(stage2, AlertUtility.alertTypeError, message1, callback);
            return;
        }


        List<SalesRegisterDTO> filteredData = new ArrayList<>();
        for (SalesRegisterDTO salesRegData : originalData) {
            Double dobCred = Double.parseDouble(salesRegData.getCredit());
            Double dobDeb = Double.parseDouble(salesRegData.getDebit());
            if ((dobCred >= fromAmount && dobCred <= toAmount) || (dobDeb >= fromAmount && dobDeb <= toAmount)) {
                salesRegisterLogger.debug("Get filterAmountData SuccessFully Executed...");
                filteredData.add(salesRegData);
//                tblPurRegList.setItems(purRegData);
            }

        }

        tblSalesRegList.setItems(FXCollections.observableList(filteredData));
        calculateTotalAmount();
        salesRegisterLogger.debug("Get filterAmountData End...");
    }

    private void resetFields() {
        //        body.clear();
        tfSalesRegListSearch.setText("");
        tfFromAmt.setText("");
        tfToAmt.setText("");
//        body.put("duration",duration);
        getSalesRegisterData();
    }

    private void handleFilterComboBoxAction(ActionEvent event) {
        selectedFilter = cmbSalesRegSuplInv.getSelectionModel().getSelectedItem();
        if (cmbSalesRegSuplInv.getSelectionModel().getSelectedItem() == "Supplier") {
            selectedFilter = "supplier";
        } else if (cmbSalesRegSuplInv.getSelectionModel().getSelectedItem() == "Voucher No") {
            selectedFilter = "voucher";
        }
        getSalesRegDataByFilter();
    }


    private void handleDurationComboBoxAction(Event event) {
        selectedDuration = cmbSalesRegDuration.getSelectionModel().getSelectedItem();
        if (selectedDuration == "This Month") {
            duration = "month";
        } else if (selectedDuration == "Last Month") {
            duration = "lastMonth";
        } else if (selectedDuration == "Half Year") {
            duration = "halfYear";
        } else if (selectedDuration == "Full Year") {
            duration = "year";
            GlobalController.getInstance().addTabStatic(SALES_REGISTER_YEARLY_SLUG, false);
        }
        getSalesRegisterData();
    }

    private void getSalesRegDataByFilter() {
        ObservableList<SalesRegisterDTO> filteredData = FXCollections.observableArrayList();

        if (selectedFilter.equalsIgnoreCase("supplier")) {
            tfSalesRegListSearch.textProperty().addListener((observable, oldValue, newValue) -> {
                filterData(newValue.trim(), SalesRegisterDTO::getLedger);
            });
            calculateTotalAmount();
        } else if (selectedFilter.equalsIgnoreCase("voucher")) {
            tfSalesRegListSearch.textProperty().addListener((observable, oldValue, newValue) -> {
                filterData(newValue.trim(), SalesRegisterDTO::getVoucherNo);
            });
            calculateTotalAmount();
        }
    }

    private void filterData(String keyword, Function<SalesRegisterDTO, String> fieldExtractor) {
        ObservableList<SalesRegisterDTO> filteredData = FXCollections.observableArrayList();
        salesRegisterLogger.error("Search Sales Register List in SalesRegisterController");
        if (keyword.isEmpty()) {
            tblSalesRegList.setItems(originalData);
            calculateTotalAmount();
            return;
        }
        for (SalesRegisterDTO item : originalData) {
            if (matchesKeyword(item, keyword, fieldExtractor)) {
                filteredData.add(item);
            }
        }
        tblSalesRegList.setItems(filteredData);
        calculateTotalAmount();
    }

    private boolean matchesKeyword(SalesRegisterDTO item, String keyword, Function<SalesRegisterDTO, String> fieldExtractor) {
        String lowerCaseKeyword = keyword.toLowerCase();
        String fieldValue = fieldExtractor.apply(item).toLowerCase();
        return fieldValue.contains(lowerCaseKeyword);
    }

    private void calculateTotalAmount() {
        double totalCredit = 0.0;
        double totalDebit = 0.0;
        for (SalesRegisterDTO credit : tblSalesRegList.getItems()) {
            Double creditAmt = Double.parseDouble(credit.getCredit());
            totalCredit += creditAmt;
            Double debitAmt = Double.parseDouble(credit.getDebit());
            totalDebit += debitAmt;
        }

        lblTotalCredit.setText(String.valueOf(totalCredit));
        lblTotalDebit.setText(String.valueOf(totalDebit));
    }

    private void getSalesRegisterData() {
        APIClient apiClient = null;
        salesRegisterLogger.debug("Get getSalesRegisterData Started...");
        Map<String, String> body = new HashMap<>();
        try {
//            System.out.println("selectedDuration --->>> " + duration);
            if(!duration.isEmpty()){
                body.put("duration", duration);
            }

            else if (!tfSalesRegListFromDate.getText().equalsIgnoreCase("") && !tfSalesRegListToDate.getText().equalsIgnoreCase("")) {
                System.out.println("date sdf ");
                body.put("start_date", Communicator.text_to_date.fromString(tfSalesRegListFromDate.getText()).toString());
                body.put("end_date", Communicator.text_to_date.fromString(tfSalesRegListToDate.getText()).toString());
            }
//            System.out.println("body --->>> " + body);
            String formData = Globals.mapToStringforFormData(body);
            apiClient = new APIClient(EndPoints.getSalesRegisterData, formData, RequestType.FORM_DATA);
            apiClient.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    jsonObject = new Gson().fromJson(workerStateEvent.getSource().getValue().toString(), JsonObject.class);
//                    System.out.println("response =======> " + jsonObject);
                    ObservableList<SalesRegisterDTO> observableList = FXCollections.observableArrayList();
                    if (jsonObject.get("responseStatus").getAsInt() == 200) {
                        salesRegisterLogger.debug("Get getSalesRegisterData Success...");
                        JsonArray responseArray = jsonObject.get("debit").getAsJsonArray();
//                        System.out.println("responseArray--------> " + responseArray);
                        String st_dt = (jsonObject.get("d_start_date").getAsString());
                        LocalDate start_date = LocalDate.parse(st_dt);
                        String en_dt = jsonObject.get("d_end_date").getAsString();
                        LocalDate end_date = LocalDate.parse(en_dt);

                        tfSalesRegListFromDate.setText(start_date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                        tfSalesRegListToDate.setText(end_date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

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
                                observableList.add(new SalesRegisterDTO(id, date, ledger, voucherType, voucherNo, debit, credit));
                            }

                            tblSalesRegDate.setCellValueFactory(new PropertyValueFactory<>("date"));
                            tblSalesRegLedger.setCellValueFactory(new PropertyValueFactory<>("ledger"));
                            tblSalesRegVoucherType.setCellValueFactory(new PropertyValueFactory<>("voucherType"));
                            tblSalesRegVoucherNo.setCellValueFactory(new PropertyValueFactory<>("voucherNo"));
                            tblSalesRegDebitAmt.setCellValueFactory(new PropertyValueFactory<>("debit"));
                            tblSalesRegCreditAmt.setCellValueFactory(new PropertyValueFactory<>("credit"));
                            tblSalesRegList.setItems(observableList);
                            originalData = observableList;
                            calculateTotalAmount();
                            duration="";
                        } else {
                            salesRegisterLogger.debug("Get getSalesRegisterData responseObject is null...");
                            tblSalesRegList.getItems().clear();
                            duration="";
                        }
                    }
                }
            });

            apiClient.setOnFailed(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    salesRegisterLogger.error("Network API cancelled in getSalesRegisterData()" + workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.setOnCancelled(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    salesRegisterLogger.error("Network API cancelled in getSalesRegisterData()" + workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.start();
            salesRegisterLogger.debug("Get getSalesRegisterData End...");

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            salesRegisterLogger.error("Exception in getSalesRegisterData():" + exceptionAsString);
        } finally {
            apiClient = null;
        }
    }

}
