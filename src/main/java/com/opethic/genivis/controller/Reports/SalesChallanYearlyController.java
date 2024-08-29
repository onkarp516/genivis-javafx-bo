package com.opethic.genivis.controller.Reports;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.opethic.genivis.dto.Reports.SalesChallanYearlyListDTO;
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

import static com.opethic.genivis.utils.FxmFileConstants.SALES_CHALLAN_SLUG;

public class SalesChallanYearlyController implements Initializable {
    @FXML
    private BorderPane bpSalesChallanYrRootpane;
    @FXML
    private TextField tfSalesChallanYrListSearch, tfSalesChallanYrListFromDate, tfSalesChallanYrListToDate, tfSalesChallanYrFromAmt, tfSalesChallanYrToAmt;
    @FXML
    private Button btSalesChallanYrReset, btExportPdf, btExportExcel, btExportCsv, btExportPrint;
    @FXML
    private TableView<SalesChallanYearlyListDTO> tblvSalesChallanYrList;
    @FXML
    private TableColumn<SalesChallanYearlyListDTO, String> tblcSalesChallanYrMonth, tblcSalesChallanYrTransactions, tblcSalesChallanYrDebitAmt, tblcSalesChallanYrCreditAmt, tblcSalesChallanYrClosingBalance, tblcSalesChallanYrTypeCD;
    @FXML
    private Label lblSalesChallanYrTotalDebit, lblSalesChallanYrTotalCredit,lblSalesChallanYrTotalClosing;

    private ObservableList<SalesChallanYearlyListDTO> orgData;

    String message = "";
    private JsonObject jsonObject = null;

    public static final Logger salesChallanYearlyLogger = LoggerFactory.getLogger(SalesChallanYearlyController.class);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> {
            tfSalesChallanYrListSearch.requestFocus();
        });

        bpSalesChallanYrRootpane.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
//                System.out.println("Esc Clicked....!");
                GlobalController.getInstance().addTabStatic(SALES_CHALLAN_SLUG, false);
            }
        });

        //TextField Converted into DateField
        DateValidator.applyDateFormat(tfSalesChallanYrListFromDate);
        DateValidator.applyDateFormat(tfSalesChallanYrListToDate);

        // getAll Data MonthWise
        getSalesChallanYearlyData();

        // SearcField Filter
        tfSalesChallanYrListSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filterSearchData();
        });

        // Amountwise Filter after changing amount in the ToAMount field
        tfSalesChallanYrToAmt.textProperty().addListener((observable, oldValue, newValue) -> {
            filterAmountSalesChallanYrData();
        });

        btSalesChallanYrReset.setOnAction(e->{
            resetSalesChallanYrFields();
        });

        // Setting the handler for each node
        setKeyPressedHandler(tfSalesChallanYrListSearch);
        setKeyPressedHandler(tfSalesChallanYrListFromDate);
        setKeyPressedHandler(tfSalesChallanYrListToDate);
        setKeyPressedHandler(tfSalesChallanYrFromAmt);
        setKeyPressedHandler(tfSalesChallanYrToAmt);
        setKeyPressedHandler(btSalesChallanYrReset);
        setKeyPressedHandler(btExportPdf);
        setKeyPressedHandler(btExportExcel);
        setKeyPressedHandler(btExportCsv);
        setKeyPressedHandler(btExportPrint);
        tblvSalesChallanYrList.requestFocus();

        tfSalesChallanYrListToDate.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                System.out.println("DateFilter From Date " + tfSalesChallanYrListFromDate.getText().toString());
                System.out.println("DateFilter To Date " + tfSalesChallanYrListToDate.getText().toString());
                if (!tfSalesChallanYrListFromDate.getText().equalsIgnoreCase("") && !tfSalesChallanYrListToDate.getText().equalsIgnoreCase("")) {
                    getSalesChallanYearlyData();
                }
                Node nextNode = CommonTraversa.getNextFocusableNode(tfSalesChallanYrFromAmt.getScene());
                if (nextNode != null) {
                    nextNode.requestFocus();
                }
            }
            if (event.getCode() == KeyCode.TAB) {
                if (!tfSalesChallanYrListFromDate.getText().equalsIgnoreCase("") && !tfSalesChallanYrListToDate.getText().equalsIgnoreCase("")) {
                    getSalesChallanYearlyData();
                }
            }
        });

        btSalesChallanYrReset.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> updateButtonStyle(isNowFocused || btSalesChallanYrReset.isHover()));
        btSalesChallanYrReset.hoverProperty().addListener((obs, wasHovered, isNowHovered) -> updateButtonStyle(isNowHovered || btSalesChallanYrReset.isFocused()));
    }

    private void updateButtonStyle(boolean isActive) {
        if (isActive) {
            btSalesChallanYrReset.setStyle("-fx-border-width: 2; -fx-border-color: #00a0f5;");
        } else {
            btSalesChallanYrReset.setStyle("-fx-border-width: 0;");
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

    private void resetSalesChallanYrFields() {
        tfSalesChallanYrListSearch.setText("");
        tfSalesChallanYrFromAmt.setText("");
        tfSalesChallanYrToAmt.setText("");
        getSalesChallanYearlyData();
    }

    private void filterAmountSalesChallanYrData() {
        salesChallanYearlyLogger.debug("Get filterAmountData Started...");
        String fromAmountText = tfSalesChallanYrFromAmt.getText();
        String toAmountText = tfSalesChallanYrToAmt.getText();
        if (toAmountText.isEmpty()) {
            tblvSalesChallanYrList.setItems(FXCollections.observableList(orgData));
            calculateTotalSalesChallanYrAmount();
            return;
        }

        double fromAmount;
        double toAmount;
        String message = "Please Enter Valid Numeric Amounts.";
        Stage stage2 = (Stage) bpSalesChallanYrRootpane.getScene().getWindow();
        AlertUtility.CustomCallback callback = (number) -> {
            if (number == 1) {
            }
        };
        try {

        } catch (NumberFormatException e) {
            salesChallanYearlyLogger.error("Get filterAmountData NumberFormatException...");
            AlertUtility.AlertError(stage2, AlertUtility.alertTypeError, message, callback);
            return;
        }
        List<SalesChallanYearlyListDTO> filteredData = new ArrayList<>();
        fromAmount = Double.parseDouble(fromAmountText);
        toAmount = Double.parseDouble(toAmountText);
//        System.out.println(" hello From amt : "+ fromAmount + "  to amt : "+ toAmount);
        for (SalesChallanYearlyListDTO purChallanData : orgData) {
            Double dobClos = Double.parseDouble(purChallanData.getClosingBalance());
            if ((dobClos >= fromAmount && dobClos <= toAmount)) {
//                System.out.println("hello I am here");
                salesChallanYearlyLogger.debug("Get filterAmountData SuccessFully Executed...");
                filteredData.add(purChallanData);
            }

        }

        tblvSalesChallanYrList.setItems(FXCollections.observableList(filteredData));
        calculateTotalSalesChallanYrAmount();
        salesChallanYearlyLogger.debug("Get filterAmountData End...");
    }

    private void filterSearchData() {
        salesChallanYearlyLogger.debug("Get filterSearchData Started...");
        String monFilter = tfSalesChallanYrListSearch.getText().toLowerCase();
        if (tfSalesChallanYrListSearch.getText().isEmpty()) {
            tblvSalesChallanYrList.setItems(FXCollections.observableList(orgData));
            calculateTotalSalesChallanYrAmount();
            return;
        }

        List<SalesChallanYearlyListDTO> filteredData = new ArrayList<>();
        for (SalesChallanYearlyListDTO pruChallanData : orgData) {
            String monText = pruChallanData.getMonth().toLowerCase();
            if (monText.contains(monFilter)) {
                salesChallanYearlyLogger.debug("Get filterSearchData SuccessFully Executed...");
                filteredData.add(pruChallanData);
            }
        }

        tblvSalesChallanYrList.setItems(FXCollections.observableList(filteredData));
        calculateTotalSalesChallanYrAmount();
        salesChallanYearlyLogger.debug("Get filterSearchData End...");
    }

    private void getSalesChallanYearlyData() {
        APIClient apiClient = null;
        salesChallanYearlyLogger.debug("Get getSalesChallanYearlyData Started...");
        Map<String, String> body = new HashMap<>();
        try {
            if (!tfSalesChallanYrListFromDate.getText().equalsIgnoreCase("") && !tfSalesChallanYrListToDate.getText().equalsIgnoreCase("")) {
                System.out.println("date sdf ");
                body.put("start_date", Communicator.text_to_date.fromString(tfSalesChallanYrListFromDate.getText()).toString());
                body.put("end_date", Communicator.text_to_date.fromString(tfSalesChallanYrListToDate.getText()).toString());
            }
            String formData = Globals.mapToStringforFormData(body);
//            System.out.println("FormData : >>"+ formData);
            apiClient = new APIClient(EndPoints.getSalesChallanYearlyData, formData, RequestType.FORM_DATA);
            apiClient.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    jsonObject = new Gson().fromJson(workerStateEvent.getSource().getValue().toString(), JsonObject.class);
                    ObservableList<SalesChallanYearlyListDTO> observableList = FXCollections.observableArrayList();
                    if (jsonObject.get("responseStatus").getAsInt() == 200) {
                        JsonArray responseArray = jsonObject.get("response").getAsJsonArray();
                        salesChallanYearlyLogger.debug("Get getSalesChallanYearlyData Success...");
                        String st_dt = (jsonObject.get("d_start_date").getAsString());
                        LocalDate start_date = LocalDate.parse(st_dt);
                        String en_dt = jsonObject.get("d_end_date").getAsString();
                        LocalDate end_date = LocalDate.parse(en_dt);
                        tfSalesChallanYrListFromDate.setText(start_date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                        tfSalesChallanYrListToDate.setText(end_date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

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
                                observableList.add(new SalesChallanYearlyListDTO(month, debit, credit, closingBalance, type, startDate, endDate));
                            }

                            tblcSalesChallanYrMonth.setCellValueFactory(new PropertyValueFactory<>("month"));
                            tblcSalesChallanYrDebitAmt.setCellValueFactory(new PropertyValueFactory<>("debit"));
                            tblcSalesChallanYrCreditAmt.setCellValueFactory(new PropertyValueFactory<>("credit"));
                            tblcSalesChallanYrClosingBalance.setCellValueFactory(new PropertyValueFactory<>("closingBalance"));
                            tblcSalesChallanYrTypeCD.setCellValueFactory(new PropertyValueFactory<>("type"));
                            tblvSalesChallanYrList.setItems(observableList);
                            orgData = observableList;
                            calculateTotalSalesChallanYrAmount();
                        }  else {
                            salesChallanYearlyLogger.debug("Get getPurchaseChallanYearlyList responseObject is null...");
                        }
                    }
                }
            });

            apiClient.setOnFailed(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    salesChallanYearlyLogger.error("Network API cancelled in getSalesChallanYearlyData()" + workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.setOnCancelled(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    salesChallanYearlyLogger.error("Network API cancelled in getSalesChallanYearlyData()" + workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.start();
            salesChallanYearlyLogger.debug("Get getSalesChallanYearlyData End...");

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            salesChallanYearlyLogger.error("Exception in getPurchaseChallanYearlyData():" + exceptionAsString);
        } finally {
            apiClient = null;
        }
    }

    public void calculateTotalSalesChallanYrAmount(){
        double totalCredit = 0.0;
        double totalDebit = 0.0;
        double totalClosing = 0.0;
        for (SalesChallanYearlyListDTO credit : tblvSalesChallanYrList.getItems()) {
            Double creditAmt = Double.parseDouble(credit.getCredit());
            totalCredit += creditAmt;
            Double debitAmt = Double.parseDouble(credit.getDebit());
            totalDebit += debitAmt;
            Double closingAmt = Double.parseDouble(credit.getClosingBalance());
            totalClosing += closingAmt;
        }
        lblSalesChallanYrTotalCredit.setText(String.valueOf(totalCredit));
        lblSalesChallanYrTotalDebit.setText(String.valueOf(totalDebit));
        lblSalesChallanYrTotalClosing.setText(String.valueOf(totalClosing));
    }
}
