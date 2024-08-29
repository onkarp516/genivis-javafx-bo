package com.opethic.genivis.controller.GSTR1;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.opethic.genivis.controller.tranx_sales.SalesOrderListController;
import com.opethic.genivis.dto.GSTR1.GSTR1CreditDebitUnRegisteredDTO;
import com.opethic.genivis.dto.GSTR2.GSTR2B2BOtherTaxableInvoice2DTO;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import static com.opethic.genivis.utils.FxmFileConstants.GSTR1_DASHBOARD_LIST_SLUG;
public class GSTR1CreditDebitUnRegisteredController implements Initializable {

    @FXML
    private TableView<GSTR1CreditDebitUnRegisteredDTO> tvGSTR1CreditDebitUnRegistered;
    @FXML
    TableColumn<GSTR1CreditDebitUnRegisteredDTO, String> tcCreditDebitUnRegisteredSrNo, tcCreditDebitUnRegisteredDates, tcCreditDebitUnRegisteredInvoiceNo,
            tcCreditDebitUnRegisteredParticulars, tcCreditDebitUnRegisteredVoucherType, tcCreditDebitUnRegisteredTaxableAmt, tcCreditDebitUnRegisteredIGSTAmt,
            tcCreditDebitUnRegisteredCGSTAmt, tcCreditDebitUnRegisteredSGSTAmt, tcCreditDebitUnRegisteredCessAmt, tcCreditDebitUnRegisteredTaxAmt, tcCreditDebitUnRegisteredInvoiceAmt;
    @FXML
    private TextField dpGSTR1NilRatedFromDate, dpGSTR1NilRatedToDate, tfGSTR1NilRatedSearch;
    @FXML
    private Label labelGSTR1NilRatedNilRated, labelGSTR1NilRatedExempted;
    @FXML
    private Button tfGSTR1NilRatedUpload;

    private Node[] focusableNodes;

    private ObservableList<GSTR1CreditDebitUnRegisteredDTO> originalData;
    // Assuming this format is compatible with your API
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    @FXML
    private BorderPane spRootNilRatedPane;
    private LocalDate fromDate, toDate;
    private String responseBody, message;

    private JsonObject jsonObject = null;

    private static final Logger logger = LogManager.getLogger(SalesOrderListController.class);
    ObservableList<GSTR1CreditDebitUnRegisteredDTO> gstr1nilratedObservableList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //Set the TableView Columns with proper height and Width
        tvGSTR1CreditDebitUnRegistered.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        GSTR1NilRatedTableDesign();
        Platform.runLater(() -> dpGSTR1NilRatedFromDate.requestFocus());
        //Populate focusable nodes array with the desired order and Focus To next and Previous Element
        focusableNodes = new Node[]{dpGSTR1NilRatedFromDate, dpGSTR1NilRatedToDate, tfGSTR1NilRatedSearch, tfGSTR1NilRatedUpload, tvGSTR1CreditDebitUnRegistered};
        CommonValidationsUtils.setupFocusNavigation(focusableNodes);

        getGSTR1NilRatedData("");
        spRootNilRatedPane.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
//                System.out.println("Esc Clicked....!");
                GlobalController.getInstance().addTabStatic(GSTR1_DASHBOARD_LIST_SLUG, false);
            }
        });
        DateValidator.applyDateFormat(dpGSTR1NilRatedFromDate);
        DateValidator.applyDateFormat(dpGSTR1NilRatedToDate);
        sceneInitilization();

        dpGSTR1NilRatedFromDate.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER){
                dpGSTR1NilRatedToDate.requestFocus();
            }
        });

        dpGSTR1NilRatedToDate.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                fromDate = null;
                toDate = null;
                if (!dpGSTR1NilRatedFromDate.getText().isEmpty()) {
                    fromDate = Communicator.text_to_date.fromString(dpGSTR1NilRatedFromDate.getText());
                }
                if (!dpGSTR1NilRatedToDate.getText().isEmpty()) {
                    toDate = Communicator.text_to_date.fromString(dpGSTR1NilRatedToDate.getText());
                }
                getGSTR1NilRatedData("");
                tfGSTR1NilRatedSearch.requestFocus();
            }
            if (event.getCode() == KeyCode.TAB) {
                fromDate = null;
                toDate = null;
                if (!dpGSTR1NilRatedFromDate.getText().isEmpty()) {
                    fromDate = Communicator.text_to_date.fromString(dpGSTR1NilRatedFromDate.getText());
                }
                if (!dpGSTR1NilRatedToDate.getText().isEmpty()) {
                    toDate = Communicator.text_to_date.fromString(dpGSTR1NilRatedToDate.getText());
                }
                getGSTR1NilRatedData("");
            }
        });

        //todo:Search without API Call in the Table
        originalData = tvGSTR1CreditDebitUnRegistered.getItems();
        tfGSTR1NilRatedSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filterData(newValue.trim());
        });
    }

    public void sceneInitilization() {
        spRootNilRatedPane.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null && newScene.getWindow() instanceof Stage) {
                Communicator.stage = (Stage) newScene.getWindow();
            }
        });
    }
    private void filterData(String keyword) {
        ObservableList<GSTR1CreditDebitUnRegisteredDTO> filteredData = FXCollections.observableArrayList();

        if (keyword.isEmpty()) {
            tvGSTR1CreditDebitUnRegistered.setItems(originalData);
            calculateTotalAmount(); // Calculate totals for all records
            return;
        }

        for (GSTR1CreditDebitUnRegisteredDTO item : originalData) {
            if (matchesKeyword(item, keyword)) {
                filteredData.add(item);
            }
        }

        tvGSTR1CreditDebitUnRegistered.setItems(filteredData);
        calculateTotalAmount(); // Calculate totals for filtered records
    }

    //todo:Search Function to Search in the Table for columns
    private boolean matchesKeyword(GSTR1CreditDebitUnRegisteredDTO item, String keyword) {
        String lowerCaseKeyword = keyword.toLowerCase();

        // todo:Check if any of the columns contain the keyword
        return
            item.getId().toLowerCase().contains(lowerCaseKeyword) ||
            item.getSr_no().toLowerCase().contains(lowerCaseKeyword) ||
            item.getInvoice_no().toLowerCase().contains(lowerCaseKeyword) ||
            item.getParticulars().toLowerCase().contains(lowerCaseKeyword) ||
            item.getVoucher_type().toLowerCase().contains(lowerCaseKeyword) ||
            item.getTaxable_amt().toLowerCase().contains(lowerCaseKeyword) ||
            item.getIgst_amt().toLowerCase().contains(lowerCaseKeyword) ||
            item.getCgst_amt().toLowerCase().contains(lowerCaseKeyword) ||
            item.getSgst_amt().toLowerCase().contains(lowerCaseKeyword) ||
            item.getCess_amt().toLowerCase().contains(lowerCaseKeyword) ||
            item.getTax_amt().toLowerCase().contains(lowerCaseKeyword) ||
            item.getInvoice_amt().toLowerCase().contains(lowerCaseKeyword);
    }

    private void calculateTotalAmount()
    {
        ObservableList<GSTR1CreditDebitUnRegisteredDTO> filteredData = tvGSTR1CreditDebitUnRegistered.getItems();
        // Calculate the Totals
        double totalTax_amt = 0.0;
        double totalInvoice_amt = 0.0;
        for (GSTR1CreditDebitUnRegisteredDTO item : filteredData) {
            totalTax_amt += Double.parseDouble(item.getTax_amt().isEmpty() ? "0.0" : item.getTax_amt());
            totalInvoice_amt += Double.parseDouble(item.getInvoice_amt().isEmpty() ? "0.0" : item.getInvoice_amt());
        }

        // Update Labels in the FXML
        labelGSTR1NilRatedNilRated.setText(String.format("%.2f", totalTax_amt));
        labelGSTR1NilRatedExempted.setText(String.format("%.2f", totalInvoice_amt));
    }

    //


    //Design of table to display the data
    public void GSTR1NilRatedTableDesign() {
        //todo: Responsive code for bottom productInfo tableview
        tcCreditDebitUnRegisteredSrNo.prefWidthProperty().bind(tvGSTR1CreditDebitUnRegistered.widthProperty().multiply(0.03));
        tcCreditDebitUnRegisteredDates.prefWidthProperty().bind(tvGSTR1CreditDebitUnRegistered.widthProperty().multiply(0.05));
        tcCreditDebitUnRegisteredInvoiceNo.prefWidthProperty().bind(tvGSTR1CreditDebitUnRegistered.widthProperty().multiply(0.06));
        tcCreditDebitUnRegisteredParticulars.prefWidthProperty().bind(tvGSTR1CreditDebitUnRegistered.widthProperty().multiply(0.12));
        tcCreditDebitUnRegisteredVoucherType.prefWidthProperty().bind(tvGSTR1CreditDebitUnRegistered.widthProperty().multiply(0.07));
        tcCreditDebitUnRegisteredTaxableAmt.prefWidthProperty().bind(tvGSTR1CreditDebitUnRegistered.widthProperty().multiply(0.07));
        tcCreditDebitUnRegisteredIGSTAmt.prefWidthProperty().bind(tvGSTR1CreditDebitUnRegistered.widthProperty().multiply(0.07));
        tcCreditDebitUnRegisteredCGSTAmt.prefWidthProperty().bind(tvGSTR1CreditDebitUnRegistered.widthProperty().multiply(0.07));
        tcCreditDebitUnRegisteredSGSTAmt.prefWidthProperty().bind(tvGSTR1CreditDebitUnRegistered.widthProperty().multiply(0.07));
        tcCreditDebitUnRegisteredCessAmt.prefWidthProperty().bind(tvGSTR1CreditDebitUnRegistered.widthProperty().multiply(0.07));
        tcCreditDebitUnRegisteredTaxAmt.prefWidthProperty().bind(tvGSTR1CreditDebitUnRegistered.widthProperty().multiply(0.07));
        tcCreditDebitUnRegisteredInvoiceAmt.prefWidthProperty().bind(tvGSTR1CreditDebitUnRegistered.widthProperty().multiply(0.07));
    }

    //Get and Set the  List Data of GSTR1 Nil Rated
    public void getGSTR1NilRatedData(String searchKey) {
        APIClient apiClient = null;
//        SalesQuotationListLogger.info(" fetch Sales Quotation invoice List : SalesQuotationListController");
        try {
            gstr1nilratedObservableList.clear();
            logger.debug("Get GSTR1 Nil Rated Data Started...");
            // Format dates
            String startDate = fromDate != null ? fromDate.format(dateFormatter) : "";
            String endDate = toDate != null ? toDate.format(dateFormatter) : "";
            System.out.println("Start Date: " + startDate);
            System.out.println("End Date: " + endDate);
            Map<String, String> body = new HashMap<>();
//            body.put("pageNo", "1");
//            body.put("pageSize", "50");
//            body.put("searchText", "");
//            body.put("sort", "");
            if (startDate != "" || endDate != "") {
                body.put("start_date", startDate);
                body.put("end_date", endDate);
            }

            String requestBody = Globals.mapToStringforFormData(body);
            System.out.println("i am in" + requestBody);
//            HttpResponse<String> response = APIClient.postJsonRequest(requestBody, EndPoints.GET_GSTR1_NIL_RATED_ENDPOINT);
            apiClient = new APIClient(EndPoints.GET_GSTR1_CredDebUnreg_RATED_ENDPOINT, requestBody, RequestType.FORM_DATA);
            apiClient.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    jsonObject = new Gson().fromJson(workerStateEvent.getSource().getValue().toString(), JsonObject.class);

//                    System.out.println("SalesOrder" + response.body());
//                    JsonObject jsonObject = new Gson().fromJson(response.body(), JsonObject.class);
                    System.out.println("this is Response of Nil Rated Report" + jsonObject);
                    gstr1nilratedObservableList = FXCollections.observableArrayList();
                    if (jsonObject.get("responseStatus").getAsInt() == 200) {
                        LocalDate start_date = LocalDate.parse(jsonObject.get("start_date").getAsString());
                        LocalDate end_date = LocalDate.parse(jsonObject.get("end_date").getAsString());
                        dpGSTR1NilRatedFromDate.setText(start_date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                        dpGSTR1NilRatedToDate.setText(end_date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
//                        JsonObject responseObject = jsonObject.get("data").getAsJsonObject();
                        JsonArray responseArray = jsonObject.get("data").getAsJsonArray();
                        int countSr = 1;
                        if (responseArray.size() > 0) {
                            tvGSTR1CreditDebitUnRegistered.setVisible(true);
                            for (JsonElement element : responseArray) {
                                JsonObject item = element.getAsJsonObject();
                                System.out.println("item in nil rated list " + item);
                                String id = item.get("id").getAsString();
                                String dates = item.get("transaction_date") != null ? item.get("transaction_date").getAsString() : "";
                                String invoice_no = item.get("sales_return_no") != null ? item.get("sales_return_no").getAsString() : "";
                                String particulars = item.get("sundry_debtor_name") != null ? item.get("sundry_debtor_name").getAsString() : "";
                                String voucher_type = item.get("sales_account_name") != null ? item.get("sales_account_name").getAsString() : "";
                                String taxable_amt = item.get("taxable_amt") != null ? item.get("taxable_amt").getAsString() : "";
                                String igst_amt = item.get("total_igst") != null ? item.get("total_igst").getAsString() : "";
                                String cgst_amt = item.get("total_cgst") != null ? item.get("total_cgst").getAsString() : "";
                                String sgst_amt = item.get("total_sgst") != null ? item.get("total_sgst").getAsString() : "";
                                String cess_amt = item.get("") != null ? item.get("").getAsString() : "";
                                String tax_amt = item.get("tax_amt") != null ? item.get("tax_amt").getAsString() : "";
                                String invoice_amt = item.get("total_amount") != null ? item.get("total_amount").getAsString() : "";

                                gstr1nilratedObservableList.add(new GSTR1CreditDebitUnRegisteredDTO(String.valueOf(countSr++), id, dates, invoice_no, particulars, voucher_type, taxable_amt, igst_amt, cgst_amt, sgst_amt, cess_amt, tax_amt, invoice_amt));

                            }
                            tcCreditDebitUnRegisteredSrNo.setCellValueFactory(new PropertyValueFactory<>("sr_no"));
                            tcCreditDebitUnRegisteredDates.setCellValueFactory(new PropertyValueFactory<>("dates"));
                            tcCreditDebitUnRegisteredInvoiceNo.setCellValueFactory(new PropertyValueFactory<>("invoice_no"));
                            tcCreditDebitUnRegisteredParticulars.setCellValueFactory(new PropertyValueFactory<>("particulars"));
                            tcCreditDebitUnRegisteredVoucherType.setCellValueFactory(new PropertyValueFactory<>("voucher_type"));
                            tcCreditDebitUnRegisteredTaxableAmt.setCellValueFactory(new PropertyValueFactory<>("taxable_amt"));
                            tcCreditDebitUnRegisteredIGSTAmt.setCellValueFactory(new PropertyValueFactory<>("igst_amt"));
                            tcCreditDebitUnRegisteredCGSTAmt.setCellValueFactory(new PropertyValueFactory<>("cgst_amt"));
                            tcCreditDebitUnRegisteredSGSTAmt.setCellValueFactory(new PropertyValueFactory<>("sgst_amt"));
                            tcCreditDebitUnRegisteredCessAmt.setCellValueFactory(new PropertyValueFactory<>("cess_amt"));
                            tcCreditDebitUnRegisteredTaxAmt.setCellValueFactory(new PropertyValueFactory<>("tax_amt"));
                            tcCreditDebitUnRegisteredInvoiceAmt.setCellValueFactory(new PropertyValueFactory<>("invoice_amt"));



                            tvGSTR1CreditDebitUnRegistered.setItems(gstr1nilratedObservableList);
//                            originalData.setAll(gstr1nilratedObservableList);
                            originalData = gstr1nilratedObservableList;
                            calculateTotalAmount();

                        } else {
//                    SalesQuotationListLogger.debug("Sales Quotation invoice List ResponseObject is null : SalesQuotationListController");
                            //TODO : use logger for alert messages
                            AlertUtility.CustomCallback callback = (number) -> {
                                if (number == 0) {
                                    dpGSTR1NilRatedToDate.requestFocus();
                                }
                            };
                            Stage stage = (Stage) spRootNilRatedPane.getScene().getWindow();
                            AlertUtility.AlertError(stage, AlertUtility.alertTypeError, "Failed to Load Data ", callback);
                            gstr1nilratedObservableList.clear();
                            calculateTotalAmount();

                        }
                    } else {
//                SalesQuotationListLogger.debug("Error in response of Sales Quotation invoice List : SalesQuotationListController");
                        //TODO : use logger for alert messages
                        AlertUtility.CustomCallback callback = (number) -> {
                            if (number == 0) {
                                dpGSTR1NilRatedToDate.requestFocus();
                            }
                        };
                        Stage stage = (Stage) spRootNilRatedPane.getScene().getWindow();
                        AlertUtility.AlertError(stage, AlertUtility.alertTypeError, "Falied to connect server! ", callback);
                        gstr1nilratedObservableList.clear();
                        calculateTotalAmount();
                    }
                }
            });

            apiClient.setOnCancelled(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    logger.error("Network API cancelled in getGSTR1NilRatedData()" + workerStateEvent.getSource().getValue().toString());

                }
            });

            apiClient.setOnFailed(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    logger.error("Network API failed in getGSTR1NilRatedData()" + workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.start();
            logger.debug("Get GSTR1 Nil Rated Data End...");
        } catch (Exception e) {
            //TODO : use logger for alert messages
            AlertUtility.CustomCallback callback = (number) -> {
                if (number == 0) {
                    dpGSTR1NilRatedToDate.requestFocus();
                }
            };
            Stage stage = (Stage) spRootNilRatedPane.getScene().getWindow();
            AlertUtility.AlertError(stage, AlertUtility.alertTypeError, "Falied to connect server! ", callback);

            e.printStackTrace();
        } finally {
            apiClient = null;
        }
    }

}
