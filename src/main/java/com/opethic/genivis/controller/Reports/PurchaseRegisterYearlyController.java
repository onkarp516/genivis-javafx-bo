package com.opethic.genivis.controller.Reports;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.opethic.genivis.dto.Reports.PurchaseRegisterDTO;
import com.opethic.genivis.dto.Reports.PurchaseRegisterYearlyListDTO;
import com.opethic.genivis.dto.reqres.product.Communicator;
import com.opethic.genivis.network.APIClient;
import com.opethic.genivis.network.EndPoints;
import com.opethic.genivis.network.RequestType;
import com.opethic.genivis.utils.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
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

import static com.opethic.genivis.utils.FxmFileConstants.GSTR2_DASHBOARD_LIST_SLUG;
import static com.opethic.genivis.utils.FxmFileConstants.PURCHASE_REGISTER_SLUG;

public class PurchaseRegisterYearlyController implements Initializable {

    @FXML
    private BorderPane bpPurRegYrRootpane;
    @FXML
    private TextField tfPurRegYrListSearch, tfPurRegYrListFromDate, tfPurRegYrListToDate, tfPurRegYrFromAmt, tfPurRegYrToAmt;
    @FXML
    private Button btPurRegYrReset, btExportPdf, btExportExcel, btExportCsv, btExportPrint;
    @FXML
    private TableView  <PurchaseRegisterYearlyListDTO>  tblvPurRegYrList;
    @FXML
    private TableColumn <PurchaseRegisterYearlyListDTO, String> tblcPurRegYrMonth, tblcPurRegYrTransactions, tblcPurRegYrDebitAmt, tblcPurRegYrCreditAmt, tblcPurRegYrClosingBalance, tblcPurRegYrTypeCD;
    @FXML
    private Label lblPurRegYrTotalDebit, lblPurRegYrTotalCredit,lblPurRegYrTotalClosing;

    private ObservableList<PurchaseRegisterYearlyListDTO> orgData;

    String message = "";
    private JsonObject jsonObject = null;

    public static final Logger purchaseRegisterYearlyLogger = LoggerFactory.getLogger(PurchaseRegisterYearlyController.class);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> {
            tfPurRegYrListSearch.requestFocus();
        });

        bpPurRegYrRootpane.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
//                System.out.println("Esc Clicked....!");
                GlobalController.getInstance().addTabStatic(PURCHASE_REGISTER_SLUG, false);
            }
        });

        //TextField Converted into DateField
        DateValidator.applyDateFormat(tfPurRegYrListFromDate);
        DateValidator.applyDateFormat(tfPurRegYrListToDate);

        // After Enter date Filter
//        tfPurRegYrListToDate.setOnKeyPressed(e->{
//            if (e.getCode() == KeyCode.ENTER) {
//                if (tfPurRegYrListFromDate != null && tfPurRegYrListFromDate.getText() != null && !tfPurRegYrListFromDate.getText().isEmpty()) {
//                    body.put("start_date", Communicator.text_to_date.fromString(tfPurRegYrListFromDate.getText()).toString());
//                }
//                if (tfPurRegYrListToDate != null && tfPurRegYrListToDate.getText() != null && !tfPurRegYrListToDate.getText().isEmpty()) {
//                    body.put("end_date", Communicator.text_to_date.fromString(tfPurRegYrListToDate.getText()).toString());
//                }
//            }
//            getPurchaseRegisterYearlyData();
//        });

        // getAll Data MonthWise
        getPurchaseRegisterYearlyData();

        // SearcField Filter
        tfPurRegYrListSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filterSearchData();
        });

        // Amountwise Filter after changing amount in the ToAMount field
        tfPurRegYrToAmt.textProperty().addListener((observable, oldValue, newValue) -> {
            filterAmountPurRegYrData();
        });

        btPurRegYrReset.setOnAction(e->{
            resetPurRegYrFields();
        });

        // Setting the handler for each node
        setKeyPressedHandler(tfPurRegYrListSearch);
        setKeyPressedHandler(tfPurRegYrListFromDate);
        setKeyPressedHandler(tfPurRegYrListToDate);
        setKeyPressedHandler(tfPurRegYrFromAmt);
        setKeyPressedHandler(tfPurRegYrToAmt);
        setKeyPressedHandler(btPurRegYrReset);
        setKeyPressedHandler(btExportPdf);
        setKeyPressedHandler(btExportExcel);
        setKeyPressedHandler(btExportCsv);
        setKeyPressedHandler(btExportPrint);
        tblvPurRegYrList.requestFocus();

        tfPurRegYrListToDate.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                System.out.println("DateFilter From Date " + tfPurRegYrListFromDate.getText().toString());
                System.out.println("DateFilter To Date " + tfPurRegYrListToDate.getText().toString());
                if (!tfPurRegYrListFromDate.getText().equalsIgnoreCase("") && !tfPurRegYrListToDate.getText().equalsIgnoreCase("")) {
                    getPurchaseRegisterYearlyData();
                }
                Node nextNode = CommonTraversa.getNextFocusableNode(tfPurRegYrFromAmt.getScene());
                if (nextNode != null) {
                    nextNode.requestFocus();
                }
            }
            if (event.getCode() == KeyCode.TAB) {
                if (!tfPurRegYrListFromDate.getText().equalsIgnoreCase("") && !tfPurRegYrListToDate.getText().equalsIgnoreCase("")) {
                    getPurchaseRegisterYearlyData();
                }
            }
        });

        btPurRegYrReset.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> updateButtonStyle(isNowFocused || btPurRegYrReset.isHover()));
        btPurRegYrReset.hoverProperty().addListener((obs, wasHovered, isNowHovered) -> updateButtonStyle(isNowHovered || btPurRegYrReset.isFocused()));

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
            btPurRegYrReset.setStyle("-fx-border-width: 2; -fx-border-color: #00a0f5;");
        } else {
            btPurRegYrReset.setStyle("-fx-border-width: 0;");
        }
    }

    private void getPurchaseRegisterYearlyData() {
        APIClient apiClient = null;
        purchaseRegisterYearlyLogger.debug("Get getPurchaseRegisterYearlyData Started...");
        Map<String, String> body = new HashMap<>();
        try {
//            if (tfPurRegYrListFromDate != null && tfPurRegYrListFromDate.getText() != null && !tfPurRegYrListFromDate.getText().isEmpty()) {
//                    body.put("start_date", Communicator.text_to_date.fromString(tfPurRegYrListFromDate.getText()).toString());
//                }
//                if (tfPurRegYrListToDate != null && tfPurRegYrListToDate.getText() != null && !tfPurRegYrListToDate.getText().isEmpty()) {
//                    body.put("end_date", Communicator.text_to_date.fromString(tfPurRegYrListToDate.getText()).toString());
//                }
            if (!tfPurRegYrListFromDate.getText().equalsIgnoreCase("") && !tfPurRegYrListToDate.getText().equalsIgnoreCase("")) {
                System.out.println("date sdf ");
                body.put("start_date", Communicator.text_to_date.fromString(tfPurRegYrListFromDate.getText()).toString());
                body.put("end_date", Communicator.text_to_date.fromString(tfPurRegYrListToDate.getText()).toString());
            }
            String formData = Globals.mapToStringforFormData(body);
            System.out.println("FormData : >>"+ formData);
            apiClient = new APIClient(EndPoints.getPurchaseRegisterYearlyData, formData, RequestType.FORM_DATA);
            apiClient.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    jsonObject = new Gson().fromJson(workerStateEvent.getSource().getValue().toString(), JsonObject.class);
                    ObservableList<PurchaseRegisterYearlyListDTO> observableList = FXCollections.observableArrayList();
                    if (jsonObject.get("responseStatus").getAsInt() == 200) {
                        JsonArray responseArray = jsonObject.get("response").getAsJsonArray();
                        purchaseRegisterYearlyLogger.debug("Get getPurchaseRegisterYearlyData Success...");
                        String st_dt = (jsonObject.get("d_start_date").getAsString());
                        LocalDate start_date = LocalDate.parse(st_dt);
                        String en_dt = jsonObject.get("d_end_date").getAsString();
                        LocalDate end_date = LocalDate.parse(en_dt);
                        tfPurRegYrListFromDate.setText(start_date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                        tfPurRegYrListToDate.setText(end_date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

                        if (responseArray.size() > 0) {
                            for (JsonElement element : responseArray) {
                                JsonObject item = element.getAsJsonObject();
                                String month = item.get("month").getAsString();
                                String debit = item.get("debit").getAsString();
                                String credit = item.get("credit").getAsString();
                                String closingBalance = item.get("closing_balance").getAsString();
                                String type = item.get("type").getAsString();
                                String startDate = item.get("start_date").getAsString();
                                String endDate = item.get("end_date").getAsString();
                                observableList.add(new PurchaseRegisterYearlyListDTO(month, debit, credit, closingBalance, type, startDate, endDate));
                            }

                            tblcPurRegYrMonth.setCellValueFactory(new PropertyValueFactory<>("month"));
                            tblcPurRegYrDebitAmt.setCellValueFactory(new PropertyValueFactory<>("debit"));
                            tblcPurRegYrCreditAmt.setCellValueFactory(new PropertyValueFactory<>("credit"));
                            tblcPurRegYrClosingBalance.setCellValueFactory(new PropertyValueFactory<>("closingBalance"));
                            tblcPurRegYrTypeCD.setCellValueFactory(new PropertyValueFactory<>("type"));
                            tblvPurRegYrList.setItems(observableList);
                            orgData = observableList;
                            calculateTotalPurRegYrAmount();
                        } else {
                            purchaseRegisterYearlyLogger.debug("Get getPurchaseRegisterYearlyList responseObject is null...");
                        }
                    }
                }
            });

            apiClient.setOnFailed(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    purchaseRegisterYearlyLogger.error("Network API cancelled in getPurchaseRegisterYearlyData()" + workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.setOnCancelled(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    purchaseRegisterYearlyLogger.error("Network API cancelled in getPurchaseRegisterYearlyData()" + workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.start();
            purchaseRegisterYearlyLogger.debug("Get getPurchaseRegisterYearlyData End...");

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            purchaseRegisterYearlyLogger.error("Exception in getPurchaseRegisterYearlyData():" + exceptionAsString);
        } finally {
            apiClient = null;
        }
    }

    private void filterSearchData() {
        purchaseRegisterYearlyLogger.debug("Get filterSearchData Started...");
        String monFilter = tfPurRegYrListSearch.getText().toLowerCase();
        if (tfPurRegYrListSearch.getText().isEmpty()) {
            tblvPurRegYrList.setItems(FXCollections.observableList(orgData));
            calculateTotalPurRegYrAmount();
            return;
        }

        List<PurchaseRegisterYearlyListDTO> filteredData = new ArrayList<>();
        for (PurchaseRegisterYearlyListDTO pruRegData : orgData) {
            String monText = pruRegData.getMonth().toLowerCase();
            if (monText.contains(monFilter)) {
                purchaseRegisterYearlyLogger.debug("Get filterSearchData SuccessFully Executed...");
                filteredData.add(pruRegData);
            }
        }

        tblvPurRegYrList.setItems(FXCollections.observableList(filteredData));
        calculateTotalPurRegYrAmount();
        purchaseRegisterYearlyLogger.debug("Get filterSearchData End...");
    }

    private void filterAmountPurRegYrData() {
        purchaseRegisterYearlyLogger.debug("Get filterAmountData Started...");
        String fromAmountText = tfPurRegYrFromAmt.getText();
        String toAmountText = tfPurRegYrToAmt.getText();
        if (toAmountText.isEmpty()) {
            tblvPurRegYrList.setItems(FXCollections.observableList(orgData));
            calculateTotalPurRegYrAmount();
            return;
        }

        double fromAmount;
        double toAmount;
        String message = "Please Enter Valid Numeric Amounts.";
        Stage stage2 = (Stage) bpPurRegYrRootpane.getScene().getWindow();
        AlertUtility.CustomCallback callback = (number) -> {
            if (number == 1) {
            }
        };
        try {

        } catch (NumberFormatException e) {
            purchaseRegisterYearlyLogger.error("Get filterAmountData NumberFormatException...");
            AlertUtility.AlertError(stage2, AlertUtility.alertTypeError, message, callback);
            return;
        }
        List<PurchaseRegisterYearlyListDTO> filteredData = new ArrayList<>();
        fromAmount = Double.parseDouble(fromAmountText);
        toAmount = Double.parseDouble(toAmountText);
        System.out.println(" hello From amt : "+ fromAmount + "  to amt : "+ toAmount);
        for (PurchaseRegisterYearlyListDTO purRegData : orgData) {
            Double dobClos = Double.parseDouble(purRegData.getClosingBalance());
            if ((dobClos >= fromAmount && dobClos <= toAmount)) {
                System.out.println("hello I am here");
                purchaseRegisterYearlyLogger.debug("Get filterAmountData SuccessFully Executed...");
                filteredData.add(purRegData);
            }

        }

        tblvPurRegYrList.setItems(FXCollections.observableList(filteredData));
        calculateTotalPurRegYrAmount();
        purchaseRegisterYearlyLogger.debug("Get filterAmountData End...");
    }

    private void calculateTotalPurRegYrAmount(){
        double totalCredit = 0.0;
        double totalDebit = 0.0;
        double totalClosing = 0.0;
        for (PurchaseRegisterYearlyListDTO credit : tblvPurRegYrList.getItems()) {
            Double creditAmt = Double.parseDouble(credit.getCredit());
            totalCredit += creditAmt;
            Double debitAmt = Double.parseDouble(credit.getDebit());
            totalDebit += debitAmt;
            Double closingAmt = Double.parseDouble(credit.getClosingBalance());
            totalClosing += closingAmt;
        }
        lblPurRegYrTotalCredit.setText(String.valueOf(totalCredit));
        lblPurRegYrTotalDebit.setText(String.valueOf(totalDebit));
        lblPurRegYrTotalClosing.setText(String.valueOf(totalClosing));
    }

    private void resetPurRegYrFields(){
//        body.clear();
        tfPurRegYrListSearch.setText("");
        tfPurRegYrFromAmt.setText("");
        tfPurRegYrToAmt.setText("");
//        tfPurRegYrListToDate.setText("");
        getPurchaseRegisterYearlyData();
    }


}
