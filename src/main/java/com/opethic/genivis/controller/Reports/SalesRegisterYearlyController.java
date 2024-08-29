package com.opethic.genivis.controller.Reports;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.opethic.genivis.dto.Reports.PurchaseRegisterYearlyListDTO;
import com.opethic.genivis.dto.Reports.SalesRegisterYearlyListDTO;
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
import static com.opethic.genivis.utils.FxmFileConstants.SALES_REGISTER_SLUG;

public class SalesRegisterYearlyController implements Initializable  {

    @FXML
    private BorderPane bpSalesRegYrRootpane;
    @FXML
    private TextField tfSalesRegYrListSearch, tfSalesRegYrListFromDate, tfSalesRegYrListToDate, tfSalesRegYrFromAmt, tfSalesRegYrToAmt;
    @FXML
    private Button btSalesRegYrReset, btExportPdf, btExportExcel, btExportCsv, btExportPrint;
    @FXML
    private TableView<SalesRegisterYearlyListDTO> tblvSalesRegYrList;
    @FXML
    private TableColumn<SalesRegisterYearlyListDTO, String> tblcSalesRegYrMonth, tblcSalesRegYrTransactions, tblcSalesRegYrDebitAmt, tblcSalesRegYrCreditAmt, tblcSalesRegYrClosingBalance, tblcSalesRegYrTypeCD;
    @FXML
    private Label lblSalesRegYrTotalDebit, lblSalesRegYrTotalCredit,lblSalesRegYrTotalClosing;

    private ObservableList<SalesRegisterYearlyListDTO> orgData;

    String message = "";
    private JsonObject jsonObject = null;

    public static final Logger salesRegisterYearlyLogger = LoggerFactory.getLogger(SalesRegisterYearlyController.class);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> {
            tfSalesRegYrListSearch.requestFocus();
        });

        bpSalesRegYrRootpane.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
//                System.out.println("Esc Clicked....!");
                GlobalController.getInstance().addTabStatic(SALES_REGISTER_SLUG, false);
            }
        });

        //TextField Converted into DateField
        DateValidator.applyDateFormat(tfSalesRegYrListFromDate);
        DateValidator.applyDateFormat(tfSalesRegYrListToDate);

        // getAll Data MonthWise
        getSalesRegisterYearlyData();

        // SearcField Filter
        tfSalesRegYrListSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filterSearchData();
        });

        // Amountwise Filter after changing amount in the ToAMount field
        tfSalesRegYrToAmt.textProperty().addListener((observable, oldValue, newValue) -> {
            filterAmountSalesRegYrData();
        });

        btSalesRegYrReset.setOnAction(e->{
            resetSalesRegYrFields();
        });

        // Setting the handler for each node
        setKeyPressedHandler(tfSalesRegYrListSearch);
        setKeyPressedHandler(tfSalesRegYrListFromDate);
        setKeyPressedHandler(tfSalesRegYrListToDate);
        setKeyPressedHandler(tfSalesRegYrFromAmt);
        setKeyPressedHandler(tfSalesRegYrToAmt);
        setKeyPressedHandler(btSalesRegYrReset);
        setKeyPressedHandler(btExportPdf);
        setKeyPressedHandler(btExportExcel);
        setKeyPressedHandler(btExportCsv);
        setKeyPressedHandler(btExportPrint);
        tblvSalesRegYrList.requestFocus();

        tfSalesRegYrListToDate.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                System.out.println("DateFilter From Date " + tfSalesRegYrListFromDate.getText().toString());
                System.out.println("DateFilter To Date " + tfSalesRegYrListToDate.getText().toString());
                if (!tfSalesRegYrListFromDate.getText().equalsIgnoreCase("") && !tfSalesRegYrListToDate.getText().equalsIgnoreCase("")) {
                    getSalesRegisterYearlyData();
                }
                Node nextNode = CommonTraversa.getNextFocusableNode(tfSalesRegYrFromAmt.getScene());
                if (nextNode != null) {
                    nextNode.requestFocus();
                }
            }
            if (event.getCode() == KeyCode.TAB) {
                if (!tfSalesRegYrListFromDate.getText().equalsIgnoreCase("") && !tfSalesRegYrListToDate.getText().equalsIgnoreCase("")) {
                    getSalesRegisterYearlyData();
                }
            }
        });

        btSalesRegYrReset.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> updateButtonStyle(isNowFocused || btSalesRegYrReset.isHover()));
        btSalesRegYrReset.hoverProperty().addListener((obs, wasHovered, isNowHovered) -> updateButtonStyle(isNowHovered || btSalesRegYrReset.isFocused()));
    }

    private void updateButtonStyle(boolean isActive) {
        if (isActive) {
            btSalesRegYrReset.setStyle("-fx-border-width: 2; -fx-border-color: #00a0f5;");
        } else {
            btSalesRegYrReset.setStyle("-fx-border-width: 0;");
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

    private void resetSalesRegYrFields() {
        tfSalesRegYrListSearch.setText("");
        tfSalesRegYrFromAmt.setText("");
        tfSalesRegYrToAmt.setText("");
        getSalesRegisterYearlyData();
    }

    private void filterAmountSalesRegYrData() {
        salesRegisterYearlyLogger.debug("Get filterAmountData Started...");
        String fromAmountText = tfSalesRegYrFromAmt.getText();
        String toAmountText = tfSalesRegYrToAmt.getText();
        if (toAmountText.isEmpty()) {
            tblvSalesRegYrList.setItems(FXCollections.observableList(orgData));
            calculateTotalSalesRegYrAmount();
            return;
        }

        double fromAmount;
        double toAmount;
        String message = "Please Enter Valid Numeric Amounts.";
        Stage stage2 = (Stage) bpSalesRegYrRootpane.getScene().getWindow();
        AlertUtility.CustomCallback callback = (number) -> {
            if (number == 1) {
            }
        };
        try {

        } catch (NumberFormatException e) {
            salesRegisterYearlyLogger.error("Get filterAmountData NumberFormatException...");
            AlertUtility.AlertError(stage2, AlertUtility.alertTypeError, message, callback);
            return;
        }
        List<SalesRegisterYearlyListDTO> filteredData = new ArrayList<>();
        fromAmount = Double.parseDouble(fromAmountText);
        toAmount = Double.parseDouble(toAmountText);
//        System.out.println(" hello From amt : "+ fromAmount + "  to amt : "+ toAmount);
        for (SalesRegisterYearlyListDTO purRegData : orgData) {
            Double dobClos = Double.parseDouble(purRegData.getClosingBalance());
            if ((dobClos >= fromAmount && dobClos <= toAmount)) {
//                System.out.println("hello I am here");
                salesRegisterYearlyLogger.debug("Get filterAmountData SuccessFully Executed...");
                filteredData.add(purRegData);
            }

        }

        tblvSalesRegYrList.setItems(FXCollections.observableList(filteredData));
        calculateTotalSalesRegYrAmount();
        salesRegisterYearlyLogger.debug("Get filterAmountData End...");
    }

    private void filterSearchData() {
        salesRegisterYearlyLogger.debug("Get filterSearchData Started...");
        String monFilter = tfSalesRegYrListSearch.getText().toLowerCase();
        if (tfSalesRegYrListSearch.getText().isEmpty()) {
            tblvSalesRegYrList.setItems(FXCollections.observableList(orgData));
            calculateTotalSalesRegYrAmount();
            return;
        }

        List<SalesRegisterYearlyListDTO> filteredData = new ArrayList<>();
        for (SalesRegisterYearlyListDTO pruRegData : orgData) {
            String monText = pruRegData.getMonth().toLowerCase();
            if (monText.contains(monFilter)) {
                salesRegisterYearlyLogger.debug("Get filterSearchData SuccessFully Executed...");
                filteredData.add(pruRegData);
            }
        }

        tblvSalesRegYrList.setItems(FXCollections.observableList(filteredData));
        calculateTotalSalesRegYrAmount();
        salesRegisterYearlyLogger.debug("Get filterSearchData End...");
    }

    private void getSalesRegisterYearlyData() {
        APIClient apiClient = null;
        salesRegisterYearlyLogger.debug("Get getSalesRegisterYearlyData Started...");
        Map<String, String> body = new HashMap<>();
        try {
            if (!tfSalesRegYrListFromDate.getText().equalsIgnoreCase("") && !tfSalesRegYrListToDate.getText().equalsIgnoreCase("")) {
                System.out.println("date sdf ");
                body.put("start_date", Communicator.text_to_date.fromString(tfSalesRegYrListFromDate.getText()).toString());
                body.put("end_date", Communicator.text_to_date.fromString(tfSalesRegYrListToDate.getText()).toString());
            }
            String formData = Globals.mapToStringforFormData(body);
//            System.out.println("FormData : >>"+ formData);
            apiClient = new APIClient(EndPoints.getSalesRegisterYearlyData, formData, RequestType.FORM_DATA);
            apiClient.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    jsonObject = new Gson().fromJson(workerStateEvent.getSource().getValue().toString(), JsonObject.class);
                    ObservableList<SalesRegisterYearlyListDTO> observableList = FXCollections.observableArrayList();
                    if (jsonObject.get("responseStatus").getAsInt() == 200) {
                        JsonArray responseArray = jsonObject.get("response").getAsJsonArray();
                        salesRegisterYearlyLogger.debug("Get getSalesRegisterYearlyData Success...");
                        String st_dt = (jsonObject.get("d_start_date").getAsString());
                        LocalDate start_date = LocalDate.parse(st_dt);
                        String en_dt = jsonObject.get("d_end_date").getAsString();
                        LocalDate end_date = LocalDate.parse(en_dt);
                        tfSalesRegYrListFromDate.setText(start_date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                        tfSalesRegYrListToDate.setText(end_date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

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
                                observableList.add(new SalesRegisterYearlyListDTO(month, debit, credit, closingBalance, type, startDate, endDate));
                            }

                            tblcSalesRegYrMonth.setCellValueFactory(new PropertyValueFactory<>("month"));
                            tblcSalesRegYrDebitAmt.setCellValueFactory(new PropertyValueFactory<>("debit"));
                            tblcSalesRegYrCreditAmt.setCellValueFactory(new PropertyValueFactory<>("credit"));
                            tblcSalesRegYrClosingBalance.setCellValueFactory(new PropertyValueFactory<>("closingBalance"));
                            tblcSalesRegYrTypeCD.setCellValueFactory(new PropertyValueFactory<>("type"));
                            tblvSalesRegYrList.setItems(observableList);
                            orgData = observableList;
                            calculateTotalSalesRegYrAmount();
                        }  else {
                            salesRegisterYearlyLogger.debug("Get getPurchaseRegisterYearlyList responseObject is null...");
                        }
                    }
                }
            });

            apiClient.setOnFailed(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    salesRegisterYearlyLogger.error("Network API cancelled in getSalesRegisterYearlyData()" + workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.setOnCancelled(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    salesRegisterYearlyLogger.error("Network API cancelled in getSalesRegisterYearlyData()" + workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.start();
            salesRegisterYearlyLogger.debug("Get getSalesRegisterYearlyData End...");

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            salesRegisterYearlyLogger.error("Exception in getPurchaseRegisterYearlyData():" + exceptionAsString);
        } finally {
            apiClient = null;
        }
    }

    public void calculateTotalSalesRegYrAmount(){
        double totalCredit = 0.0;
        double totalDebit = 0.0;
        double totalClosing = 0.0;
        for (SalesRegisterYearlyListDTO credit : tblvSalesRegYrList.getItems()) {
            Double creditAmt = Double.parseDouble(credit.getCredit());
            totalCredit += creditAmt;
            Double debitAmt = Double.parseDouble(credit.getDebit());
            totalDebit += debitAmt;
            Double closingAmt = Double.parseDouble(credit.getClosingBalance());
            totalClosing += closingAmt;
        }
        lblSalesRegYrTotalCredit.setText(String.valueOf(totalCredit));
        lblSalesRegYrTotalDebit.setText(String.valueOf(totalDebit));
        lblSalesRegYrTotalClosing.setText(String.valueOf(totalClosing));
    }
}
