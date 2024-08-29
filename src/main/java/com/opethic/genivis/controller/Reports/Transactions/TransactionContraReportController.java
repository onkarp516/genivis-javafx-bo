package com.opethic.genivis.controller.Reports.Transactions;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.opethic.genivis.dto.Reports.TransactionContraDTO;
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

import static com.opethic.genivis.utils.FxmFileConstants.TRANSACTION_CONTRA_YEARLY_SLUG;

public class TransactionContraReportController implements Initializable {

    @FXML
    private BorderPane bpTransactionContraReportRootPane;
    @FXML
    private ComboBox<String> cmbContraSuplInv, cmbContraDuration;
    @FXML
    private TextField tfContraListSearch, tfContraListFromDate, tfContraListToDate, tfFromAmt, tfToAmt;
    @FXML
    private Button btReset, btExportPdf, btExportExcel, btExportCsv, btExportPrint;
    @FXML
    private TableView<TransactionContraDTO> tblContraList;
    @FXML
    private TableColumn<TransactionContraDTO, String> tblContraDate, tblContraLedger, tblContraVoucherType, tblContraVoucherNo, tblContraDebitAmt, tblContraCreditAmt;
    @FXML
    private Label lblTotalDebit, lblTotalCredit;

    String duration = "";
    String selectedDuration = "", selectedFilter = "", selectedType = "";

    private ObservableList<TransactionContraDTO> originalData;

    String message = "";
    private JsonObject jsonObject = null;

    public static final Logger transactionContraReportLogger = LoggerFactory.getLogger(TransactionContraReportController.class);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> {
            tfContraListSearch.requestFocus();
        });

        // Initial Static Values Set Supplier/Voucher  Combobox
        cmbContraSuplInv.getItems().addAll("Supplier", "Voucher");
        cmbContraSuplInv.setPromptText("Filter Search");
        cmbContraSuplInv.setOnAction(this::handleFilterComboBoxAction);
        cmbContraSuplInv.setValue("Supplier");
        handleFilterComboBoxAction(null);
        //todo:open Filter dropdown on Space
        cmbContraSuplInv.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.SPACE && !cmbContraSuplInv.isShowing()) {
                cmbContraSuplInv.show();
                event.consume();
            }
        });

        // Initial Static Values Set Duration  Combobox
        cmbContraDuration.getItems().addAll("This Month", "Last Month", "Half Year", "Full Year");
        cmbContraDuration.setPromptText("Select Duration");
        cmbContraDuration.setOnAction(this::handleDurationComboBoxAction);
        cmbContraDuration.setValue(cmbContraDuration.getItems().get(0));
        duration = "month";
        //todo:open Filter dropdown on Space
        cmbContraDuration.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.SPACE && !cmbContraDuration.isShowing()) {
                cmbContraDuration.show();
                event.consume();
            }
        });

        getTractionContraReportData();

        // Amountwise Filter after changing amount in the ToAMount field
        tfToAmt.textProperty().addListener((observable, oldValue, newValue) -> {
            filterAmountData();
        });

        btReset.setOnAction(e -> {
            resetFields();
        });

        // TextFields Converting in DateField
        DateValidator.applyDateFormat(tfContraListFromDate);
        DateValidator.applyDateFormat(tfContraListToDate);

        // Setting the handler for each node
        setKeyPressedHandler(cmbContraSuplInv);
        setKeyPressedHandler(tfContraListSearch);
        setKeyPressedHandler(cmbContraDuration);
        setKeyPressedHandler(tfContraListFromDate);
        setKeyPressedHandler(tfContraListToDate);
        setKeyPressedHandler(tfFromAmt);
        setKeyPressedHandler(tfToAmt);
        setKeyPressedHandler(btReset);
        setKeyPressedHandler(btExportPdf);
        setKeyPressedHandler(btExportExcel);
        setKeyPressedHandler(btExportCsv);
        setKeyPressedHandler(btExportPrint);
        tblContraList.requestFocus();

        tfContraListToDate.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                System.out.println("DateFilter From Date " + tfContraListFromDate.getText().toString());
                System.out.println("DateFilter To Date " + tfContraListToDate.getText().toString());
                if (!tfContraListFromDate.getText().equalsIgnoreCase("") && !tfContraListToDate.getText().equalsIgnoreCase("")) {
                    getTractionContraReportData();
                }
                Node nextNode = CommonTraversa.getNextFocusableNode(tfFromAmt.getScene());
                if (nextNode != null) {
                    nextNode.requestFocus();
                }
            }
            if (event.getCode() == KeyCode.TAB) {
                if (!tfContraListFromDate.getText().equalsIgnoreCase("") && !tfContraListToDate.getText().equalsIgnoreCase("")) {
                    getTractionContraReportData();
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
        selectedFilter = cmbContraSuplInv.getSelectionModel().getSelectedItem();
        if (cmbContraSuplInv.getSelectionModel().getSelectedItem() == "Supplier") {
            selectedFilter = "supplier";
        } else if (cmbContraSuplInv.getSelectionModel().getSelectedItem() == "Voucher No") {
            selectedFilter = "voucher";
        }
        getContraDataByFilter();
    }

    private void getContraDataByFilter() {
        ObservableList<TransactionContraDTO> filteredData = FXCollections.observableArrayList();

        if (selectedFilter.equalsIgnoreCase("supplier")) {
            tfContraListSearch.textProperty().addListener((observable, oldValue, newValue) -> {
                filterData(newValue.trim(), TransactionContraDTO::getLedger);
            });
            calculateTotalAmount();
        } else if (selectedFilter.equalsIgnoreCase("voucher")) {
            tfContraListSearch.textProperty().addListener((observable, oldValue, newValue) -> {
                filterData(newValue.trim(), TransactionContraDTO::getVoucherNo);
            });
            calculateTotalAmount();
        }
    }

    private void filterData(String keyword, Function<TransactionContraDTO, String> fieldExtractor) {
        ObservableList<TransactionContraDTO> filteredData = FXCollections.observableArrayList();
        transactionContraReportLogger.error("Search Transaction Contra Report List in TransactionContraReportController");
        if (keyword.isEmpty()) {
            tblContraList.setItems(originalData);
            calculateTotalAmount();
            return;
        }
        for (TransactionContraDTO item : originalData) {
            if (matchesKeyword(item, keyword, fieldExtractor)) {
                filteredData.add(item);
            }
        }
        tblContraList.setItems(filteredData);
        calculateTotalAmount();
    }

    private boolean matchesKeyword(TransactionContraDTO item, String keyword, Function<TransactionContraDTO, String> fieldExtractor) {
        String lowerCaseKeyword = keyword.toLowerCase();
        String fieldValue = fieldExtractor.apply(item).toLowerCase();
        return fieldValue.contains(lowerCaseKeyword);
    }

    private void handleDurationComboBoxAction(Event event) {
        selectedDuration = cmbContraDuration.getSelectionModel().getSelectedItem();
        if (selectedDuration == "This Month") {
            duration = "month";
        } else if (selectedDuration == "Last Month") {
            duration = "lastMonth";
        } else if (selectedDuration == "Half Year") {
            duration = "halfYear";
        } else if (selectedDuration == "Full Year") {
            duration = "year";
            GlobalController.getInstance().addTabStatic(TRANSACTION_CONTRA_YEARLY_SLUG, false);
        }
        getTractionContraReportData();

    }


    private void getTractionContraReportData() {
        APIClient apiClient = null;
        transactionContraReportLogger.debug("Get getTractionContraReportData Started...");
        Map<String, String> body = new HashMap<>(); // this used for mapping the data to call getTractionContraReportData() as per requirement  sending the Formdata
        try {
            System.out.println("selectedDuration --->>> " + duration);
            if (!duration.isEmpty()) {
                body.put("duration", duration);
            } else if (!tfContraListFromDate.getText().equalsIgnoreCase("") && !tfContraListToDate.getText().equalsIgnoreCase("")) {
                System.out.println("date sdf ");
                body.put("start_date", Communicator.text_to_date.fromString(tfContraListFromDate.getText()).toString());
                body.put("end_date", Communicator.text_to_date.fromString(tfContraListToDate.getText()).toString());
            }
            System.out.println("body --->>> " + body);
            String formData = Globals.mapToStringforFormData(body);
            apiClient = new APIClient(EndPoints.getTractionContraReportData, formData, RequestType.FORM_DATA);
            apiClient.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    jsonObject = new Gson().fromJson(workerStateEvent.getSource().getValue().toString(), JsonObject.class);
                    System.out.println("response =======> " + jsonObject);
                    ObservableList<TransactionContraDTO> observableList = FXCollections.observableArrayList();
                    if (jsonObject.get("responseStatus").getAsInt() == 200) {
                        transactionContraReportLogger.debug("Get getTractionContraReportData Success...");
                        JsonArray responseArray = jsonObject.get("response").getAsJsonArray();
//                        System.out.println("responseArray--------> " + responseArray);
                        String st_dt = (jsonObject.get("d_start_date").getAsString());
                        LocalDate start_date = LocalDate.parse(st_dt);
                        String en_dt = jsonObject.get("d_end_date").getAsString();
                        LocalDate end_date = LocalDate.parse(en_dt);

                        tfContraListFromDate.setText(start_date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                        tfContraListToDate.setText(end_date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

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
                                observableList.add(new TransactionContraDTO(id, date, ledger, voucherType, voucherNo, debit, credit));
                            }


                            tblContraDate.setCellValueFactory(new PropertyValueFactory<>("date"));
                            tblContraLedger.setCellValueFactory(new PropertyValueFactory<>("ledger"));
                            tblContraVoucherType.setCellValueFactory(new PropertyValueFactory<>("voucherType"));
                            tblContraVoucherNo.setCellValueFactory(new PropertyValueFactory<>("voucherNo"));
                            tblContraDebitAmt.setCellValueFactory(new PropertyValueFactory<>("debit"));
                            tblContraCreditAmt.setCellValueFactory(new PropertyValueFactory<>("credit"));
                            tblContraList.setItems(observableList);
                            originalData = observableList;
                            calculateTotalAmount();
                            duration = "";
                        } else {
                            transactionContraReportLogger.debug("Get getTractionContraReportData responseObject is null...");
                            tblContraList.getItems().clear();
                            duration = "";
                        }
//                        setCreditNoteFormData(creditNoteByIdMainDTO);
                    }
                }
            });

            apiClient.setOnFailed(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    transactionContraReportLogger.error("Network API cancelled in getTractionContraReportData()" + workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.setOnCancelled(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    transactionContraReportLogger.error("Network API cancelled in getTractionContraReportData()" + workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.start();
            transactionContraReportLogger.debug("Get getTractionContraReportData End...");

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            transactionContraReportLogger.error("Exception in getTractionContraReportData():" + exceptionAsString);
        } finally {
            apiClient = null;
        }
    }

    private void filterAmountData() {
        transactionContraReportLogger.debug("Get filterAmountData Started...");
        String fromAmountText = tfFromAmt.getText();
        String toAmountText = tfToAmt.getText();
        if (toAmountText.isEmpty()) {
            tblContraList.setItems(FXCollections.observableList(originalData));
            calculateTotalAmount();
            return;
        }

        double fromAmount;
        double toAmount;
        String message = "Please Enter a Valid Amount Range.";
        String message1 = "Please Enter Valid Numeric Amounts.";
        Stage stage2 = (Stage) bpTransactionContraReportRootPane.getScene().getWindow();
        AlertUtility.CustomCallback callback = (number) -> {
            if (number == 1) {
            }
        };
        try {
            fromAmount = Double.parseDouble(fromAmountText);
            toAmount = Double.parseDouble(toAmountText);
        } catch (NumberFormatException e) {
            transactionContraReportLogger.error("Get filterAmountData NumberFormatException...");
            AlertUtility.AlertError(stage2, AlertUtility.alertTypeError, message1, callback);
            return;
        }


        List<TransactionContraDTO> filteredData = new ArrayList<>();
        for (TransactionContraDTO contraData : originalData) {
            Double dobCred = Double.parseDouble(contraData.getCredit());
            Double dobDeb = Double.parseDouble(contraData.getDebit());
            if ((dobCred >= fromAmount && dobCred <= toAmount) || (dobDeb >= fromAmount && dobDeb <= toAmount)) {
                transactionContraReportLogger.debug("Get filterAmountData SuccessFully Executed...");
                filteredData.add(contraData);
//                tblContraList.setItems(contraData);
            }

        }

        tblContraList.setItems(FXCollections.observableList(filteredData));
        calculateTotalAmount();
        transactionContraReportLogger.debug("Get filterAmountData End...");
    }

    private void calculateTotalAmount() {
        double totalCredit = 0.0;
        double totalDebit = 0.0;
        for (TransactionContraDTO credit : tblContraList.getItems()) {
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
        tfContraListSearch.setText("");
        tfFromAmt.setText("");
        tfToAmt.setText("");
//        body.put("duration",duration);
        getTractionContraReportData();
    }

}
