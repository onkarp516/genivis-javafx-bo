package com.opethic.genivis.controller.Reports.Transactions;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.opethic.genivis.dto.Reports.TransactionContraYearlyListDTO;
import com.opethic.genivis.dto.reqres.product.Communicator;
import com.opethic.genivis.network.APIClient;
import com.opethic.genivis.network.EndPoints;
import com.opethic.genivis.network.RequestType;
import com.opethic.genivis.utils.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
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

import static com.opethic.genivis.utils.FxmFileConstants.TRANSACTION_CONTRA_SLUG;

public class TransactionContraReportYearlyController implements Initializable {

    @FXML
    private BorderPane bpContraYrRootpane;
    @FXML
    private TextField tfContraYrListSearch, tfContraYrListFromDate, tfContraYrListToDate, tfContraYrFromAmt, tfContraYrToAmt;
    @FXML
    private Button btContraYrReset, btExportPdf, btExportExcel, btExportCsv, btExportPrint;
    @FXML
    private TableView<TransactionContraYearlyListDTO> tblvContraYrList;
    @FXML
    private TableColumn<TransactionContraYearlyListDTO, String> tblcContraYrMonth, tblcContraYrTransactions, tblcContraYrVouchers, tblcContraYrAmount;
    @FXML
    private Label lblContraYrTotalDebit, lblContraYrTotalCredit,lblContraYrTotalClosing;

    private ObservableList<TransactionContraYearlyListDTO> orgData;

    String message = "";
    private JsonObject jsonObject = null;

    public static final Logger transactionContraReportYearlyLogger = LoggerFactory.getLogger(TransactionContraReportYearlyController.class);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> {
            tfContraYrListSearch.requestFocus();
        });

        bpContraYrRootpane.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
//                System.out.println("Esc Clicked....!");
                GlobalController.getInstance().addTabStatic(TRANSACTION_CONTRA_SLUG, false);
            }
        });

        //TextField Converted into DateField
        DateValidator.applyDateFormat(tfContraYrListFromDate);
        DateValidator.applyDateFormat(tfContraYrListToDate);

        // getAll Data MonthWise
        getTracnsactionContraReportYearlyData();

        // SearcField Filter
        tfContraYrListSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filterSearchData();
        });

        // Amountwise Filter after changing amount in the ToAMount field
//        tfContraYrToAmt.textProperty().addListener((observable, oldValue, newValue) -> {
//            filterAmountContraYrData();
//        });

//        btContraYrReset.setOnAction(e->{
//            resetContraYrFields();
//        });

        // Setting the handler for each node
        setKeyPressedHandler(tfContraYrListSearch);
        setKeyPressedHandler(tfContraYrListFromDate);
        setKeyPressedHandler(tfContraYrListToDate);
//        setKeyPressedHandler(tfContraYrFromAmt);
//        setKeyPressedHandler(tfContraYrToAmt);
//        setKeyPressedHandler(btContraYrReset);
        setKeyPressedHandler(btExportPdf);
        setKeyPressedHandler(btExportExcel);
        setKeyPressedHandler(btExportCsv);
        setKeyPressedHandler(btExportPrint);
        tblvContraYrList.requestFocus();

        tfContraYrListToDate.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                System.out.println("DateFilter From Date " + tfContraYrListFromDate.getText().toString());
                System.out.println("DateFilter To Date " + tfContraYrListToDate.getText().toString());
                if (!tfContraYrListFromDate.getText().equalsIgnoreCase("") && !tfContraYrListToDate.getText().equalsIgnoreCase("")) {
                    getTracnsactionContraReportYearlyData();
                }
                Node nextNode = CommonTraversa.getNextFocusableNode(btExportPdf.getScene());
                if (nextNode != null) {
                    nextNode.requestFocus();
                }
            }
            if (event.getCode() == KeyCode.TAB) {
                if (!tfContraYrListFromDate.getText().equalsIgnoreCase("") && !tfContraYrListToDate.getText().equalsIgnoreCase("")) {
                    getTracnsactionContraReportYearlyData();
                }
            }
        });

//        btContraYrReset.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> updateButtonStyle(isNowFocused || btContraYrReset.isHover()));
//        btContraYrReset.hoverProperty().addListener((obs, wasHovered, isNowHovered) -> updateButtonStyle(isNowHovered || btContraYrReset.isFocused()));

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

//    private void updateButtonStyle(boolean isActive) {
//        if (isActive) {
//            btContraYrReset.setStyle("-fx-border-width: 2; -fx-border-color: #00a0f5;");
//        } else {
//            btContraYrReset.setStyle("-fx-border-width: 0;");
//        }
//    }

    private void getTracnsactionContraReportYearlyData() {
        APIClient apiClient = null;
        transactionContraReportYearlyLogger.debug("Get getTracnsactionContraReportYearlyData Started...");
        Map<String, String> body = new HashMap<>();
        try {
//            if (tfContraYrListFromDate != null && tfContraYrListFromDate.getText() != null && !tfContraYrListFromDate.getText().isEmpty()) {
//                    body.put("start_date", Communicator.text_to_date.fromString(tfContraYrListFromDate.getText()).toString());
//                }
//                if (tfContraYrListToDate != null && tfContraYrListToDate.getText() != null && !tfContraYrListToDate.getText().isEmpty()) {
//                    body.put("end_date", Communicator.text_to_date.fromString(tfContraYrListToDate.getText()).toString());
//                }
            if (!tfContraYrListFromDate.getText().equalsIgnoreCase("") && !tfContraYrListToDate.getText().equalsIgnoreCase("")) {
                System.out.println("date sdf ");
                body.put("start_date", Communicator.text_to_date.fromString(tfContraYrListFromDate.getText()).toString());
                body.put("end_date", Communicator.text_to_date.fromString(tfContraYrListToDate.getText()).toString());
            }
            String formData = Globals.mapToStringforFormData(body);
            System.out.println("FormData : >>"+ formData);
            apiClient = new APIClient(EndPoints.getTractionContraReportYearlyData, formData, RequestType.FORM_DATA);
            apiClient.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    jsonObject = new Gson().fromJson(workerStateEvent.getSource().getValue().toString(), JsonObject.class);
                    System.out.println("response =======> " + jsonObject);
                    ObservableList<TransactionContraYearlyListDTO> observableList = FXCollections.observableArrayList();
                    if (jsonObject.get("responseStatus").getAsInt() == 200) {
                        JsonArray responseArray = jsonObject.get("response").getAsJsonArray();
                        transactionContraReportYearlyLogger.debug("Get getTracnsactionContraReportYearlyData Success...");
                        System.out.println("responseArray--------> " + responseArray);
                        String st_dt = (jsonObject.get("d_start_date").getAsString());
                        LocalDate start_date = LocalDate.parse(st_dt);
                        String en_dt = jsonObject.get("d_end_date").getAsString();
                        LocalDate end_date = LocalDate.parse(en_dt);
                        tfContraYrListFromDate.setText(start_date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                        tfContraYrListToDate.setText(end_date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

                        if (responseArray.size() > 0) {
                            for (JsonElement element : responseArray) {
                                JsonObject item = element.getAsJsonObject();
                                String month = item.get("month").getAsString();
                                String vouchers = item.get("no_vouchers").getAsString();
                                String amount = item.get("total_amt").getAsString();
                                observableList.add(new TransactionContraYearlyListDTO(month, vouchers, amount));
                            }

                            tblcContraYrMonth.setCellValueFactory(new PropertyValueFactory<>("month"));
                            tblcContraYrVouchers.setCellValueFactory(new PropertyValueFactory<>("vouchers"));
                            tblcContraYrAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
                            tblvContraYrList.setItems(observableList);
                            orgData = observableList;
                            calculateTotalContraYrAmount();
                        } else {
                            transactionContraReportYearlyLogger.debug("Get getTransactionContraReportsYearlyList responseObject is null...");
                        }
                    }
                }
            });

            apiClient.setOnFailed(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    transactionContraReportYearlyLogger.error("Network API cancelled in getTracnsactionContraReportYearlyData()" + workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.setOnCancelled(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    transactionContraReportYearlyLogger.error("Network API cancelled in getTracnsactionContraReportYearlyData()" + workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.start();
            transactionContraReportYearlyLogger.debug("Get getTracnsactionContraReportYearlyData End...");

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            transactionContraReportYearlyLogger.error("Exception in getTracnsactionContraReportYearlyData():" + exceptionAsString);
        } finally {
            apiClient = null;
        }
    }

    private void filterSearchData() {
        transactionContraReportYearlyLogger.debug("Get filterSearchData Started...");
        String monFilter = tfContraYrListSearch.getText().toLowerCase();
        if (tfContraYrListSearch.getText().isEmpty()) {
            tblvContraYrList.setItems(FXCollections.observableList(orgData));
            calculateTotalContraYrAmount();
            return;
        }

        List<TransactionContraYearlyListDTO> filteredData = new ArrayList<>();
        for (TransactionContraYearlyListDTO pruRegData : orgData) {
            String monText = pruRegData.getMonth().toLowerCase();
            if (monText.contains(monFilter)) {
                transactionContraReportYearlyLogger.debug("Get filterSearchData SuccessFully Executed...");
                filteredData.add(pruRegData);
            }
        }

        tblvContraYrList.setItems(FXCollections.observableList(filteredData));
        calculateTotalContraYrAmount();
        transactionContraReportYearlyLogger.debug("Get filterSearchData End...");
    }

//    private void filterAmountContraYrData() {
//        transactionContraReportYearlyLogger.debug("Get filterAmountData Started...");
//        String fromAmountText = tfContraYrFromAmt.getText();
//        String toAmountText = tfContraYrToAmt.getText();
//        if (toAmountText.isEmpty()) {
//            tblvContraYrList.setItems(FXCollections.observableList(orgData));
//            calculateTotalContraYrAmount();
//            return;
//        }
//
//        double fromAmount;
//        double toAmount;
//        String message = "Please Enter Valid Numeric Amounts.";
//        Stage stage2 = (Stage) bpContraYrRootpane.getScene().getWindow();
//        AlertUtility.CustomCallback callback = (number) -> {
//            if (number == 1) {
//            }
//        };
//        try {
//
//        } catch (NumberFormatException e) {
//            transactionContraReportYearlyLogger.error("Get filterAmountData NumberFormatException...");
//            AlertUtility.AlertError(stage2, AlertUtility.alertTypeError, message, callback);
//            return;
//        }
//        List<TransactionContraYearlyListDTO> filteredData = new ArrayList<>();
//        fromAmount = Double.parseDouble(fromAmountText);
//        toAmount = Double.parseDouble(toAmountText);
//        System.out.println(" hello From amt : "+ fromAmount + "  to amt : "+ toAmount);
//        for (TransactionContraYearlyListDTO contraData : orgData) {
//            Double dobClos = Double.parseDouble(contraData.getClosingBalance());
//            if ((dobClos >= fromAmount && dobClos <= toAmount)) {
//                System.out.println("hello I am here");
//                transactionContraReportYearlyLogger.debug("Get filterAmountData SuccessFully Executed...");
//                filteredData.add(contraData);
//            }
//
//        }
//
//        tblvContraYrList.setItems(FXCollections.observableList(filteredData));
//        calculateTotalContraYrAmount();
//        transactionContraReportYearlyLogger.debug("Get filterAmountData End...");
//    }

    private void calculateTotalContraYrAmount(){
        double totalVouchers = 0.0;
        double totalAmount = 0.0;
        for (TransactionContraYearlyListDTO credit : tblvContraYrList.getItems()) {
            Double creditAmt = Double.parseDouble(credit.getVouchers());
            totalVouchers += creditAmt;
            Double debitAmt = Double.parseDouble(credit.getAmount());
            totalAmount += debitAmt;
        }
        lblContraYrTotalCredit.setText(String.valueOf(totalVouchers));
        lblContraYrTotalClosing.setText(String.valueOf(totalAmount));
    }

//    private void resetContraYrFields(){
////        body.clear();
//        tfContraYrListSearch.setText("");
////        tfContraYrFromAmt.setText("");
////        tfContraYrToAmt.setText("");
////        tfContraYrListToDate.setText("");
//        getTracnsactionContraReportYearlyData();
//    }

}
