package com.opethic.genivis.controller.Reports;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.opethic.genivis.dto.Reports.SalesOrderYearlyListDTO;
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

import static com.opethic.genivis.utils.FxmFileConstants.SALES_ORDER_SLUG;

public class SalesOrderYearlyController implements Initializable {
    @FXML
    private BorderPane bpSalesOrderYrRootpane;
    @FXML
    private TextField tfSalesOrderYrListSearch, tfSalesOrderYrListFromDate, tfSalesOrderYrListToDate, tfSalesOrderYrFromAmt, tfSalesOrderYrToAmt;
    @FXML
    private Button btSalesOrderYrReset, btExportPdf, btExportExcel, btExportCsv, btExportPrint;
    @FXML
    private TableView<SalesOrderYearlyListDTO> tblvSalesOrderYrList;
    @FXML
    private TableColumn<SalesOrderYearlyListDTO, String> tblcSalesOrderYrMonth, tblcSalesOrderYrTransactions, tblcSalesOrderYrDebitAmt, tblcSalesOrderYrCreditAmt, tblcSalesOrderYrClosingBalance, tblcSalesOrderYrTypeCD;
    @FXML
    private Label lblSalesOrderYrTotalDebit, lblSalesOrderYrTotalCredit,lblSalesOrderYrTotalClosing;

    private ObservableList<SalesOrderYearlyListDTO> orgData;

    String message = "";
    private JsonObject jsonObject = null;

    public static final Logger salesOrderYearlyLogger = LoggerFactory.getLogger(SalesOrderYearlyController.class);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> {
            tfSalesOrderYrListSearch.requestFocus();
        });

        bpSalesOrderYrRootpane.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
//                System.out.println("Esc Clicked....!");
                GlobalController.getInstance().addTabStatic(SALES_ORDER_SLUG, false);
            }
        });

        //TextField Converted into DateField
        DateValidator.applyDateFormat(tfSalesOrderYrListFromDate);
        DateValidator.applyDateFormat(tfSalesOrderYrListToDate);

        // getAll Data MonthWise
        getSalesOrderYearlyData();

        // SearcField Filter
        tfSalesOrderYrListSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filterSearchData();
        });

        // Amountwise Filter after changing amount in the ToAMount field
        tfSalesOrderYrToAmt.textProperty().addListener((observable, oldValue, newValue) -> {
            filterAmountSalesOrderYrData();
        });

        btSalesOrderYrReset.setOnAction(e->{
            resetSalesOrderYrFields();
        });

        // Setting the handler for each node
        setKeyPressedHandler(tfSalesOrderYrListSearch);
        setKeyPressedHandler(tfSalesOrderYrListFromDate);
        setKeyPressedHandler(tfSalesOrderYrListToDate);
        setKeyPressedHandler(tfSalesOrderYrFromAmt);
        setKeyPressedHandler(tfSalesOrderYrToAmt);
        setKeyPressedHandler(btSalesOrderYrReset);
        setKeyPressedHandler(btExportPdf);
        setKeyPressedHandler(btExportExcel);
        setKeyPressedHandler(btExportCsv);
        setKeyPressedHandler(btExportPrint);
        tblvSalesOrderYrList.requestFocus();

        tfSalesOrderYrListToDate.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                System.out.println("DateFilter From Date " + tfSalesOrderYrListFromDate.getText().toString());
                System.out.println("DateFilter To Date " + tfSalesOrderYrListToDate.getText().toString());
                if (!tfSalesOrderYrListFromDate.getText().equalsIgnoreCase("") && !tfSalesOrderYrListToDate.getText().equalsIgnoreCase("")) {
                    getSalesOrderYearlyData();
                }
                Node nextNode = CommonTraversa.getNextFocusableNode(tfSalesOrderYrFromAmt.getScene());
                if (nextNode != null) {
                    nextNode.requestFocus();
                }
            }
            if (event.getCode() == KeyCode.TAB) {
                if (!tfSalesOrderYrListFromDate.getText().equalsIgnoreCase("") && !tfSalesOrderYrListToDate.getText().equalsIgnoreCase("")) {
                    getSalesOrderYearlyData();
                }
            }
        });

        btSalesOrderYrReset.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> updateButtonStyle(isNowFocused || btSalesOrderYrReset.isHover()));
        btSalesOrderYrReset.hoverProperty().addListener((obs, wasHovered, isNowHovered) -> updateButtonStyle(isNowHovered || btSalesOrderYrReset.isFocused()));
    }

    private void updateButtonStyle(boolean isActive) {
        if (isActive) {
            btSalesOrderYrReset.setStyle("-fx-border-width: 2; -fx-border-color: #00a0f5;");
        } else {
            btSalesOrderYrReset.setStyle("-fx-border-width: 0;");
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

    private void resetSalesOrderYrFields() {
        tfSalesOrderYrListSearch.setText("");
        tfSalesOrderYrFromAmt.setText("");
        tfSalesOrderYrToAmt.setText("");
        getSalesOrderYearlyData();
    }

    private void filterAmountSalesOrderYrData() {
        salesOrderYearlyLogger.debug("Get filterAmountData Started...");
        String fromAmountText = tfSalesOrderYrFromAmt.getText();
        String toAmountText = tfSalesOrderYrToAmt.getText();
        if (toAmountText.isEmpty()) {
            tblvSalesOrderYrList.setItems(FXCollections.observableList(orgData));
            calculateTotalSalesOrderYrAmount();
            return;
        }

        double fromAmount;
        double toAmount;
        String message = "Please Enter Valid Numeric Amounts.";
        Stage stage2 = (Stage) bpSalesOrderYrRootpane.getScene().getWindow();
        AlertUtility.CustomCallback callback = (number) -> {
            if (number == 1) {
            }
        };
        try {

        } catch (NumberFormatException e) {
            salesOrderYearlyLogger.error("Get filterAmountData NumberFormatException...");
            AlertUtility.AlertError(stage2, AlertUtility.alertTypeError, message, callback);
            return;
        }
        List<SalesOrderYearlyListDTO> filteredData = new ArrayList<>();
        fromAmount = Double.parseDouble(fromAmountText);
        toAmount = Double.parseDouble(toAmountText);
//        System.out.println(" hello From amt : "+ fromAmount + "  to amt : "+ toAmount);
        for (SalesOrderYearlyListDTO purOrderData : orgData) {
            Double dobClos = Double.parseDouble(purOrderData.getClosingBalance());
            if ((dobClos >= fromAmount && dobClos <= toAmount)) {
//                System.out.println("hello I am here");
                salesOrderYearlyLogger.debug("Get filterAmountData SuccessFully Executed...");
                filteredData.add(purOrderData);
            }

        }

        tblvSalesOrderYrList.setItems(FXCollections.observableList(filteredData));
        calculateTotalSalesOrderYrAmount();
        salesOrderYearlyLogger.debug("Get filterAmountData End...");
    }

    private void filterSearchData() {
        salesOrderYearlyLogger.debug("Get filterSearchData Started...");
        String monFilter = tfSalesOrderYrListSearch.getText().toLowerCase();
        if (tfSalesOrderYrListSearch.getText().isEmpty()) {
            tblvSalesOrderYrList.setItems(FXCollections.observableList(orgData));
            calculateTotalSalesOrderYrAmount();
            return;
        }

        List<SalesOrderYearlyListDTO> filteredData = new ArrayList<>();
        for (SalesOrderYearlyListDTO pruOrderData : orgData) {
            String monText = pruOrderData.getMonth().toLowerCase();
            if (monText.contains(monFilter)) {
                salesOrderYearlyLogger.debug("Get filterSearchData SuccessFully Executed...");
                filteredData.add(pruOrderData);
            }
        }

        tblvSalesOrderYrList.setItems(FXCollections.observableList(filteredData));
        calculateTotalSalesOrderYrAmount();
        salesOrderYearlyLogger.debug("Get filterSearchData End...");
    }

    private void getSalesOrderYearlyData() {
        APIClient apiClient = null;
        salesOrderYearlyLogger.debug("Get getSalesOrderYearlyData Started...");
        Map<String, String> body = new HashMap<>();
        try {
            if (!tfSalesOrderYrListFromDate.getText().equalsIgnoreCase("") && !tfSalesOrderYrListToDate.getText().equalsIgnoreCase("")) {
                System.out.println("date sdf ");
                body.put("start_date", Communicator.text_to_date.fromString(tfSalesOrderYrListFromDate.getText()).toString());
                body.put("end_date", Communicator.text_to_date.fromString(tfSalesOrderYrListToDate.getText()).toString());
            }
            String formData = Globals.mapToStringforFormData(body);
//            System.out.println("FormData : >>"+ formData);
            apiClient = new APIClient(EndPoints.getSalesOrderYearlyData, formData, RequestType.FORM_DATA);
            apiClient.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    jsonObject = new Gson().fromJson(workerStateEvent.getSource().getValue().toString(), JsonObject.class);
                    ObservableList<SalesOrderYearlyListDTO> observableList = FXCollections.observableArrayList();
                    if (jsonObject.get("responseStatus").getAsInt() == 200) {
                        JsonArray responseArray = jsonObject.get("response").getAsJsonArray();
                        salesOrderYearlyLogger.debug("Get getSalesOrderYearlyData Success...");
                        String st_dt = (jsonObject.get("d_start_date").getAsString());
                        LocalDate start_date = LocalDate.parse(st_dt);
                        String en_dt = jsonObject.get("d_end_date").getAsString();
                        LocalDate end_date = LocalDate.parse(en_dt);
                        tfSalesOrderYrListFromDate.setText(start_date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                        tfSalesOrderYrListToDate.setText(end_date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

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
                                observableList.add(new SalesOrderYearlyListDTO(month, debit, credit, closingBalance, type, startDate, endDate));
                            }

                            tblcSalesOrderYrMonth.setCellValueFactory(new PropertyValueFactory<>("month"));
                            tblcSalesOrderYrDebitAmt.setCellValueFactory(new PropertyValueFactory<>("debit"));
                            tblcSalesOrderYrCreditAmt.setCellValueFactory(new PropertyValueFactory<>("credit"));
                            tblcSalesOrderYrClosingBalance.setCellValueFactory(new PropertyValueFactory<>("closingBalance"));
                            tblcSalesOrderYrTypeCD.setCellValueFactory(new PropertyValueFactory<>("type"));
                            tblvSalesOrderYrList.setItems(observableList);
                            orgData = observableList;
                            calculateTotalSalesOrderYrAmount();
                        }  else {
                            salesOrderYearlyLogger.debug("Get getPurchaseOrderYearlyList responseObject is null...");
                        }
                    }
                }
            });

            apiClient.setOnFailed(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    salesOrderYearlyLogger.error("Network API cancelled in getSalesOrderYearlyData()" + workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.setOnCancelled(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    salesOrderYearlyLogger.error("Network API cancelled in getSalesOrderYearlyData()" + workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.start();
            salesOrderYearlyLogger.debug("Get getSalesOrderYearlyData End...");

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            salesOrderYearlyLogger.error("Exception in getPurchaseOrderYearlyData():" + exceptionAsString);
        } finally {
            apiClient = null;
        }
    }

    public void calculateTotalSalesOrderYrAmount(){
        double totalCredit = 0.0;
        double totalDebit = 0.0;
        double totalClosing = 0.0;
        for (SalesOrderYearlyListDTO credit : tblvSalesOrderYrList.getItems()) {
            Double creditAmt = Double.parseDouble(credit.getCredit());
            totalCredit += creditAmt;
            Double debitAmt = Double.parseDouble(credit.getDebit());
            totalDebit += debitAmt;
            Double closingAmt = Double.parseDouble(credit.getClosingBalance());
            totalClosing += closingAmt;
        }
        lblSalesOrderYrTotalCredit.setText(String.valueOf(totalCredit));
        lblSalesOrderYrTotalDebit.setText(String.valueOf(totalDebit));
        lblSalesOrderYrTotalClosing.setText(String.valueOf(totalClosing));
    }
}
