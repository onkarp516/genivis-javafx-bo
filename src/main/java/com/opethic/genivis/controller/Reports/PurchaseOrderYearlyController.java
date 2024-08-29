package com.opethic.genivis.controller.Reports;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.opethic.genivis.dto.Reports.PurchaseOrderYearlyListDTO;
import com.opethic.genivis.dto.Reports.PurchaseRegisterYearlyListDTO;
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

import static com.opethic.genivis.utils.FxmFileConstants.PURCHASE_REGISTER_SLUG;

public class PurchaseOrderYearlyController implements Initializable {
    @FXML
    private BorderPane bpPurOrderYrRootpane;
    @FXML
    private TextField tfPurOrderYrListSearch, tfPurOrderYrListFromDate, tfPurOrderYrListToDate, tfPurOrderYrFromAmt, tfPurOrderYrToAmt;
    @FXML
    private Button btPurOrderYrReset, btExportPdf, btExportExcel, btExportCsv, btExportPrint;
    @FXML
    private TableView<PurchaseOrderYearlyListDTO> tblvPurOrderYrList;
    @FXML
    private TableColumn<PurchaseOrderYearlyListDTO, String> tblcPurOrderYrMonth, tblcPurOrderYrTransactions, tblcPurOrderYrDebitAmt, tblcPurOrderYrCreditAmt, tblcPurOrderYrClosingBalance, tblcPurOrderYrTypeCD;
    @FXML
    private Label lblPurOrderYrTotalDebit, lblPurOrderYrTotalCredit,lblPurOrderYrTotalClosing;

    private ObservableList<PurchaseOrderYearlyListDTO> orgData;

    String message = "";
    private JsonObject jsonObject = null;

    public static final Logger purchaseOrderYearlyLogger = LoggerFactory.getLogger(PurchaseOrderYearlyController.class);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> {
            tfPurOrderYrListSearch.requestFocus();
        });

        bpPurOrderYrRootpane.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
//                System.out.println("Esc Clicked....!");
                GlobalController.getInstance().addTabStatic(PURCHASE_REGISTER_SLUG, false);
            }
        });

        //TextField Converted into DateField
        DateValidator.applyDateFormat(tfPurOrderYrListFromDate);
        DateValidator.applyDateFormat(tfPurOrderYrListToDate);

        // getAll Data MonthWise
        getPurchaseOrderYearlyData();

        // SearcField Filter
        tfPurOrderYrListSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filterSearchData();
        });

        // Amountwise Filter after changing amount in the ToAMount field
        tfPurOrderYrToAmt.textProperty().addListener((observable, oldValue, newValue) -> {
            filterAmountPurOrderYrData();
        });

        btPurOrderYrReset.setOnAction(e->{
            resetPurOrderYrFields();
        });

        // Setting the handler for each node
        setKeyPressedHandler(tfPurOrderYrListSearch);
        setKeyPressedHandler(tfPurOrderYrListFromDate);
        setKeyPressedHandler(tfPurOrderYrListToDate);
        setKeyPressedHandler(tfPurOrderYrFromAmt);
        setKeyPressedHandler(tfPurOrderYrToAmt);
        setKeyPressedHandler(btPurOrderYrReset);
        setKeyPressedHandler(btExportPdf);
        setKeyPressedHandler(btExportExcel);
        setKeyPressedHandler(btExportCsv);
        setKeyPressedHandler(btExportPrint);
        tblvPurOrderYrList.requestFocus();

        tfPurOrderYrListToDate.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                System.out.println("DateFilter From Date " + tfPurOrderYrListFromDate.getText().toString());
                System.out.println("DateFilter To Date " + tfPurOrderYrListToDate.getText().toString());
                if (!tfPurOrderYrListFromDate.getText().equalsIgnoreCase("") && !tfPurOrderYrListToDate.getText().equalsIgnoreCase("")) {
                    getPurchaseOrderYearlyData();
                }
                Node nextNode = CommonTraversa.getNextFocusableNode(tfPurOrderYrFromAmt.getScene());
                if (nextNode != null) {
                    nextNode.requestFocus();
                }
            }
            if (event.getCode() == KeyCode.TAB) {
                if (!tfPurOrderYrListFromDate.getText().equalsIgnoreCase("") && !tfPurOrderYrListToDate.getText().equalsIgnoreCase("")) {
                    getPurchaseOrderYearlyData();
                }
            }
        });

        btPurOrderYrReset.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> updateButtonStyle(isNowFocused || btPurOrderYrReset.isHover()));
        btPurOrderYrReset.hoverProperty().addListener((obs, wasHovered, isNowHovered) -> updateButtonStyle(isNowHovered || btPurOrderYrReset.isFocused()));
    }


    private void filterAmountPurOrderYrData() {
        purchaseOrderYearlyLogger.debug("Get filterAmountData Started...");
        String fromAmountText = tfPurOrderYrFromAmt.getText();
        String toAmountText = tfPurOrderYrToAmt.getText();
        if (toAmountText.isEmpty()) {
            tblvPurOrderYrList.setItems(FXCollections.observableList(orgData));
            calculateTotalPurOrderYrAmount();
            return;
        }

        double fromAmount;
        double toAmount;
        String message = "Please Enter Valid Numeric Amounts.";
        Stage stage2 = (Stage) bpPurOrderYrRootpane.getScene().getWindow();
        AlertUtility.CustomCallback callback = (number) -> {
            if (number == 1) {
            }
        };
        try {

        } catch (NumberFormatException e) {
            purchaseOrderYearlyLogger.error("Get filterAmountData NumberFormatException...");
            AlertUtility.AlertError(stage2, AlertUtility.alertTypeError, message, callback);
            return;
        }
        List<PurchaseOrderYearlyListDTO> filteredData = new ArrayList<>();
        fromAmount = Double.parseDouble(fromAmountText);
        toAmount = Double.parseDouble(toAmountText);
        System.out.println(" hello From amt : "+ fromAmount + "  to amt : "+ toAmount);
        for (PurchaseOrderYearlyListDTO purRegData : orgData) {
            Double dobClos = Double.parseDouble(purRegData.getClosingBalance());
            if ((dobClos >= fromAmount && dobClos <= toAmount)) {
                System.out.println("hello I am here");
                purchaseOrderYearlyLogger.debug("Get filterAmountData SuccessFully Executed...");
                filteredData.add(purRegData);
            }

        }

        tblvPurOrderYrList.setItems(FXCollections.observableList(filteredData));
        calculateTotalPurOrderYrAmount();
        purchaseOrderYearlyLogger.debug("Get filterAmountData End...");
    }

    private void filterSearchData() {
        purchaseOrderYearlyLogger.debug("Get filterSearchData Started...");
        String monFilter = tfPurOrderYrListSearch.getText().toLowerCase();
        if (tfPurOrderYrListSearch.getText().isEmpty()) {
            tblvPurOrderYrList.setItems(FXCollections.observableList(orgData));
            calculateTotalPurOrderYrAmount();
            return;
        }

        List<PurchaseOrderYearlyListDTO> filteredData = new ArrayList<>();
        for (PurchaseOrderYearlyListDTO pruRegData : orgData) {
            String monText = pruRegData.getMonth().toLowerCase();
            if (monText.contains(monFilter)) {
                purchaseOrderYearlyLogger.debug("Get filterSearchData SuccessFully Executed...");
                filteredData.add(pruRegData);
            }
        }

        tblvPurOrderYrList.setItems(FXCollections.observableList(filteredData));
        calculateTotalPurOrderYrAmount();
        purchaseOrderYearlyLogger.debug("Get filterSearchData End...");
    }

    private void getPurchaseOrderYearlyData() {
        APIClient apiClient = null;
        purchaseOrderYearlyLogger.debug("Get getPurchaseOrderYearlyData Started...");
        Map<String, String> body = new HashMap<>();
        try {
            if (!tfPurOrderYrListFromDate.getText().equalsIgnoreCase("") && !tfPurOrderYrListToDate.getText().equalsIgnoreCase("")) {
                System.out.println("date sdf ");
                body.put("start_date", Communicator.text_to_date.fromString(tfPurOrderYrListFromDate.getText()).toString());
                body.put("end_date", Communicator.text_to_date.fromString(tfPurOrderYrListToDate.getText()).toString());
            }
            String formData = Globals.mapToStringforFormData(body);
            System.out.println("FormData : >>"+ formData);
            apiClient = new APIClient(EndPoints.getPurchaseOrderYearlyData, formData, RequestType.FORM_DATA);
            apiClient.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    jsonObject = new Gson().fromJson(workerStateEvent.getSource().getValue().toString(), JsonObject.class);
                    ObservableList<PurchaseOrderYearlyListDTO> observableList = FXCollections.observableArrayList();
                    if (jsonObject.get("responseStatus").getAsInt() == 200) {
                        JsonArray responseArray = jsonObject.get("response").getAsJsonArray();
                        purchaseOrderYearlyLogger.debug("Get getPurchaseOrderYearlyData Success...");
                        String st_dt = (jsonObject.get("d_start_date").getAsString());
                        LocalDate start_date = LocalDate.parse(st_dt);
                        String en_dt = jsonObject.get("d_end_date").getAsString();
                        LocalDate end_date = LocalDate.parse(en_dt);
                        tfPurOrderYrListFromDate.setText(start_date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                        tfPurOrderYrListToDate.setText(end_date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

                        if (responseArray.size() > 0){
                            for (JsonElement element : responseArray) {
                                JsonObject item = element.getAsJsonObject();
                                String month = item.get("month").getAsString();
                                String debit = item.get("debit").getAsString();
                                String credit = item.get("credit").getAsString();
                                String closingBalance = item.get("closing_balance").getAsString();
                                String type = item.get("type").getAsString();
                                String startDate = item.get("start_date").getAsString();
                                String endDate = item.get("end_date").getAsString();
                                observableList.add(new PurchaseOrderYearlyListDTO(month, debit, credit, closingBalance, type, startDate, endDate));
                            }

                            tblcPurOrderYrMonth.setCellValueFactory(new PropertyValueFactory<>("month"));
                            tblcPurOrderYrDebitAmt.setCellValueFactory(new PropertyValueFactory<>("debit"));
                            tblcPurOrderYrCreditAmt.setCellValueFactory(new PropertyValueFactory<>("credit"));
                            tblcPurOrderYrClosingBalance.setCellValueFactory(new PropertyValueFactory<>("closingBalance"));
                            tblcPurOrderYrTypeCD.setCellValueFactory(new PropertyValueFactory<>("type"));
                            tblvPurOrderYrList.setItems(observableList);
                            orgData = observableList;
                            calculateTotalPurOrderYrAmount();
                        } else {
                            purchaseOrderYearlyLogger.debug("Get getPurchaseOrderYearlyList responseObject is null...");
                        }
                    }
                }
            });

            apiClient.setOnFailed(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    purchaseOrderYearlyLogger.error("Network API cancelled in getPurchaseOrderYearlyData()" + workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.setOnCancelled(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    purchaseOrderYearlyLogger.error("Network API cancelled in getPurchaseOrderYearlyData()" + workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.start();
            purchaseOrderYearlyLogger.debug("Get getPurchaseOrderYearlyData End...");

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            purchaseOrderYearlyLogger.error("Exception in getPurchaseOrderYearlyData():" + exceptionAsString);
        } finally {
            apiClient = null;
        }
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
            btPurOrderYrReset.setStyle("-fx-border-width: 2; -fx-border-color: #00a0f5;");
        } else {
            btPurOrderYrReset.setStyle("-fx-border-width: 0;");
        }
    }

    private void calculateTotalPurOrderYrAmount(){
        double totalCredit = 0.0;
        double totalDebit = 0.0;
        double totalClosing = 0.0;
        for (PurchaseOrderYearlyListDTO credit : tblvPurOrderYrList.getItems()) {
            Double creditAmt = Double.parseDouble(credit.getCredit());
            totalCredit += creditAmt;
            Double debitAmt = Double.parseDouble(credit.getDebit());
            totalDebit += debitAmt;
            Double closingAmt = Double.parseDouble(credit.getClosingBalance());
            totalClosing += closingAmt;
        }
        lblPurOrderYrTotalCredit.setText(String.valueOf(totalCredit));
        lblPurOrderYrTotalDebit.setText(String.valueOf(totalDebit));
        lblPurOrderYrTotalClosing.setText(String.valueOf(totalClosing));
    }

    private void resetPurOrderYrFields(){
//        body.clear();
        tfPurOrderYrListSearch.setText("");
        tfPurOrderYrFromAmt.setText("");
        tfPurOrderYrToAmt.setText("");
//        tfPurRegYrListToDate.setText("");
        getPurchaseOrderYearlyData();
    }

}
