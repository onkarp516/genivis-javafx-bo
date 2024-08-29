package com.opethic.genivis.controller.Reports;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.opethic.genivis.dto.Reports.PurchaseChallanYearlyListDTO;
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

import static com.opethic.genivis.utils.FxmFileConstants.PURCHASE_CHALLAN_SLUG;

public class PurchaseChallanYearlyController implements Initializable {
    @FXML
    private BorderPane bpPurChallanYrRootpane;
    @FXML
    private TextField tfPurChallanYrListSearch, tfPurChallanYrListFromDate, tfPurChallanYrListToDate, tfPurChallanYrFromAmt, tfPurChallanYrToAmt;
    @FXML
    private Button btPurChallanYrReset, btExportPdf, btExportExcel, btExportCsv, btExportPrint;
    @FXML
    private TableView<PurchaseChallanYearlyListDTO> tblvPurChallanYrList;
    @FXML
    private TableColumn<PurchaseChallanYearlyListDTO, String> tblcPurChallanYrMonth, tblcPurChallanYrTransactions, tblcPurChallanYrDebitAmt, tblcPurChallanYrCreditAmt, tblcPurChallanYrClosingBalance, tblcPurChallanYrTypeCD;
    @FXML
    private Label lblPurChallanYrTotalDebit, lblPurChallanYrTotalCredit,lblPurChallanYrTotalClosing;

    private ObservableList<PurchaseChallanYearlyListDTO> orgData;

    String message = "";
    private JsonObject jsonObject = null;

    public static final Logger purchaseChallanYearlyLogger = LoggerFactory.getLogger(PurchaseChallanYearlyController.class);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> {
            tfPurChallanYrListSearch.requestFocus();
        });

        bpPurChallanYrRootpane.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
//                System.out.println("Esc Clicked....!");
                GlobalController.getInstance().addTabStatic(PURCHASE_CHALLAN_SLUG, false);
            }
        });

        //TextField Converted into DateField
        DateValidator.applyDateFormat(tfPurChallanYrListFromDate);
        DateValidator.applyDateFormat(tfPurChallanYrListToDate);

        getPurchaseChallanYearlyData();

        // SearcField Filter
        tfPurChallanYrListSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filterSearchData();
        });

        // Amountwise Filter after changing amount in the ToAMount field
        tfPurChallanYrToAmt.textProperty().addListener((observable, oldValue, newValue) -> {
            filterAmountPurChallanYrData();
        });

        btPurChallanYrReset.setOnAction(e->{
            resetPurChallanYrFields();
        });

        // Setting the handler for each node
        setKeyPressedHandler(tfPurChallanYrListSearch);
        setKeyPressedHandler(tfPurChallanYrListFromDate);
        setKeyPressedHandler(tfPurChallanYrListToDate);
        setKeyPressedHandler(tfPurChallanYrFromAmt);
        setKeyPressedHandler(tfPurChallanYrToAmt);
        setKeyPressedHandler(btPurChallanYrReset);
        setKeyPressedHandler(btExportPdf);
        setKeyPressedHandler(btExportExcel);
        setKeyPressedHandler(btExportCsv);
        setKeyPressedHandler(btExportPrint);
        tblvPurChallanYrList.requestFocus();

        tfPurChallanYrListToDate.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                System.out.println("DateFilter From Date " + tfPurChallanYrListFromDate.getText().toString());
                System.out.println("DateFilter To Date " + tfPurChallanYrListToDate.getText().toString());
                if (!tfPurChallanYrListFromDate.getText().equalsIgnoreCase("") && !tfPurChallanYrListToDate.getText().equalsIgnoreCase("")) {
                    getPurchaseChallanYearlyData();
                }
                Node nextNode = CommonTraversa.getNextFocusableNode(tfPurChallanYrFromAmt.getScene());
                if (nextNode != null) {
                    nextNode.requestFocus();
                }
            }
            if (event.getCode() == KeyCode.TAB) {
                if (!tfPurChallanYrListFromDate.getText().equalsIgnoreCase("") && !tfPurChallanYrListToDate.getText().equalsIgnoreCase("")) {
                    getPurchaseChallanYearlyData();
                }
            }
        });

        btPurChallanYrReset.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> updateButtonStyle(isNowFocused || btPurChallanYrReset.isHover()));
        btPurChallanYrReset.hoverProperty().addListener((obs, wasHovered, isNowHovered) -> updateButtonStyle(isNowHovered || btPurChallanYrReset.isFocused()));

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
            btPurChallanYrReset.setStyle("-fx-border-width: 2; -fx-border-color: #00a0f5;");
        } else {
            btPurChallanYrReset.setStyle("-fx-border-width: 0;");
        }
    }

    private void getPurchaseChallanYearlyData() {
        APIClient apiClient = null;
        purchaseChallanYearlyLogger.debug("Get getPurchaseChallanYearlyData Started...");
        Map<String, String> body = new HashMap<>();
        try {
//            if (tfPurChallanYrListFromDate != null && tfPurChallanYrListFromDate.getText() != null && !tfPurChallanYrListFromDate.getText().isEmpty()) {
//                    body.put("start_date", Communicator.text_to_date.fromString(tfPurChallanYrListFromDate.getText()).toString());
//                }
//                if (tfPurChallanYrListToDate != null && tfPurChallanYrListToDate.getText() != null && !tfPurChallanYrListToDate.getText().isEmpty()) {
//                    body.put("end_date", Communicator.text_to_date.fromString(tfPurChallanYrListToDate.getText()).toString());
//                }
            if (!tfPurChallanYrListFromDate.getText().equalsIgnoreCase("") && !tfPurChallanYrListToDate.getText().equalsIgnoreCase("")) {
                System.out.println("date sdf ");
                body.put("start_date", Communicator.text_to_date.fromString(tfPurChallanYrListFromDate.getText()).toString());
                body.put("end_date", Communicator.text_to_date.fromString(tfPurChallanYrListToDate.getText()).toString());
            }
            String formData = Globals.mapToStringforFormData(body);
            System.out.println("FormData : >>"+ formData);
            apiClient = new APIClient(EndPoints.getPurchaseChallanYearlyData, formData, RequestType.FORM_DATA);
            apiClient.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    jsonObject = new Gson().fromJson(workerStateEvent.getSource().getValue().toString(), JsonObject.class);
                    ObservableList<PurchaseChallanYearlyListDTO> observableList = FXCollections.observableArrayList();
                    if (jsonObject.get("responseStatus").getAsInt() == 200) {
                        JsonArray responseArray = jsonObject.get("response").getAsJsonArray();
                        purchaseChallanYearlyLogger.debug("Get getPurchaseChallanYearlyData Success...");
                        String st_dt = (jsonObject.get("d_start_date").getAsString());
                        LocalDate start_date = LocalDate.parse(st_dt);
                        String en_dt = jsonObject.get("d_end_date").getAsString();
                        LocalDate end_date = LocalDate.parse(en_dt);
                        tfPurChallanYrListFromDate.setText(start_date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                        tfPurChallanYrListToDate.setText(end_date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

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
                                observableList.add(new PurchaseChallanYearlyListDTO(month, debit, credit, closingBalance, type, startDate, endDate));
                            }

                            tblcPurChallanYrMonth.setCellValueFactory(new PropertyValueFactory<>("month"));
                            tblcPurChallanYrDebitAmt.setCellValueFactory(new PropertyValueFactory<>("debit"));
                            tblcPurChallanYrCreditAmt.setCellValueFactory(new PropertyValueFactory<>("credit"));
                            tblcPurChallanYrClosingBalance.setCellValueFactory(new PropertyValueFactory<>("closingBalance"));
                            tblcPurChallanYrTypeCD.setCellValueFactory(new PropertyValueFactory<>("type"));
                            tblvPurChallanYrList.setItems(observableList);
                            orgData = observableList;
                            calculateTotalPurChallanYrAmount();
                        } else {
                            purchaseChallanYearlyLogger.debug("Get getPurchaseChallanYearlyList responseObject is null...");
                        }
                    }
                }
            });

            apiClient.setOnFailed(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    purchaseChallanYearlyLogger.error("Network API cancelled in getPurchaseChallanYearlyData()" + workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.setOnCancelled(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    purchaseChallanYearlyLogger.error("Network API cancelled in getPurchaseChallanYearlyData()" + workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.start();
            purchaseChallanYearlyLogger.debug("Get getPurchaseChallanYearlyData End...");

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            purchaseChallanYearlyLogger.error("Exception in getPurchaseChallanYearlyData():" + exceptionAsString);
        } finally {
            apiClient = null;
        }
    }

    private void filterSearchData() {
        purchaseChallanYearlyLogger.debug("Get filterSearchData Started...");
        String monFilter = tfPurChallanYrListSearch.getText().toLowerCase();
        if (tfPurChallanYrListSearch.getText().isEmpty()) {
            tblvPurChallanYrList.setItems(FXCollections.observableList(orgData));
            calculateTotalPurChallanYrAmount();
            return;
        }

        List<PurchaseChallanYearlyListDTO> filteredData = new ArrayList<>();
        for (PurchaseChallanYearlyListDTO pruChallanData : orgData) {
            String monText = pruChallanData.getMonth().toLowerCase();
            if (monText.contains(monFilter)) {
                purchaseChallanYearlyLogger.debug("Get filterSearchData SuccessFully Executed...");
                filteredData.add(pruChallanData);
            }
        }

        tblvPurChallanYrList.setItems(FXCollections.observableList(filteredData));
        calculateTotalPurChallanYrAmount();
        purchaseChallanYearlyLogger.debug("Get filterSearchData End...");
    }

    private void filterAmountPurChallanYrData() {
        purchaseChallanYearlyLogger.debug("Get filterAmountData Started...");
        String fromAmountText = tfPurChallanYrFromAmt.getText();
        String toAmountText = tfPurChallanYrToAmt.getText();
        if (toAmountText.isEmpty()) {
            tblvPurChallanYrList.setItems(FXCollections.observableList(orgData));
            calculateTotalPurChallanYrAmount();
            return;
        }

        double fromAmount;
        double toAmount;
        String message = "Please Enter Valid Numeric Amounts.";
        Stage stage2 = (Stage) bpPurChallanYrRootpane.getScene().getWindow();
        AlertUtility.CustomCallback callback = (number) -> {
            if (number == 1) {
            }
        };
        try {

        } catch (NumberFormatException e) {
            purchaseChallanYearlyLogger.error("Get filterAmountData NumberFormatException...");
            AlertUtility.AlertError(stage2, AlertUtility.alertTypeError, message, callback);
            return;
        }
        List<PurchaseChallanYearlyListDTO> filteredData = new ArrayList<>();
        fromAmount = Double.parseDouble(fromAmountText);
        toAmount = Double.parseDouble(toAmountText);
        System.out.println(" hello From amt : "+ fromAmount + "  to amt : "+ toAmount);
        for (PurchaseChallanYearlyListDTO purChallanData : orgData) {
            Double dobClos = Double.parseDouble(purChallanData.getClosingBalance());
            if ((dobClos >= fromAmount && dobClos <= toAmount)) {
                System.out.println("hello I am here");
                purchaseChallanYearlyLogger.debug("Get filterAmountData SuccessFully Executed...");
                filteredData.add(purChallanData);
            }

        }

        tblvPurChallanYrList.setItems(FXCollections.observableList(filteredData));
        calculateTotalPurChallanYrAmount();
        purchaseChallanYearlyLogger.debug("Get filterAmountData End...");
    }

    private void calculateTotalPurChallanYrAmount(){
        double totalCredit = 0.0;
        double totalDebit = 0.0;
        double totalClosing = 0.0;
        for (PurchaseChallanYearlyListDTO credit : tblvPurChallanYrList.getItems()) {
            Double creditAmt = Double.parseDouble(credit.getCredit());
            totalCredit += creditAmt;
            Double debitAmt = Double.parseDouble(credit.getDebit());
            totalDebit += debitAmt;
            Double closingAmt = Double.parseDouble(credit.getClosingBalance());
            totalClosing += closingAmt;
        }
        lblPurChallanYrTotalCredit.setText(String.valueOf(totalCredit));
        lblPurChallanYrTotalDebit.setText(String.valueOf(totalDebit));
        lblPurChallanYrTotalClosing.setText(String.valueOf(totalClosing));
    }

    private void resetPurChallanYrFields(){
//        body.clear();
        tfPurChallanYrListSearch.setText("");
        tfPurChallanYrFromAmt.setText("");
        tfPurChallanYrToAmt.setText("");
//        tfPurChallanYrListToDate.setText("");
        getPurchaseChallanYearlyData();
    }


}
