package com.opethic.genivis.controller.GSTR1;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.opethic.genivis.dto.GSTR1.GSTR1B2B1DTO;
import com.opethic.genivis.dto.GSTR1.GSTR1HsnSacSummaryDTO;
import com.opethic.genivis.dto.reqres.product.Communicator;
import com.opethic.genivis.network.APIClient;
import com.opethic.genivis.network.EndPoints;
import com.opethic.genivis.network.RequestType;
import com.opethic.genivis.utils.DateValidator;
import com.opethic.genivis.utils.GlobalController;
import com.opethic.genivis.utils.Globals;
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
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import static com.opethic.genivis.utils.FxmFileConstants.*;
import static com.opethic.genivis.utils.Globals.decimalFormat;

public class GSTR1HsnSacSummaryController implements Initializable {
    private static final Logger GSTR1HsnSacSummaryListLogger = LoggerFactory.getLogger(GSTR1HsnSacSummaryController.class);
    @FXML
    private TableView<GSTR1HsnSacSummaryDTO> tblvGSTR1HsnSacSummaryList;
    @FXML
    private TableColumn<GSTR1HsnSacSummaryDTO, String> tblGSTR1HsnSacSummarySrNo, tblGSTR1HsnSacSummaryInvoiceNoHsnSac,
            tblGSTR1HsnSacSummaryDescription, tblGSTR1HsnSacSummaryTypeofSupply, tblGSTR1HsnSacSummaryTotalQuantity,
            tblGSTR1HsnSacSummaryTotalVolume, tblGSTR1HsnSacSummaryTaxRate, tblGSTR1HsnSacSummaryTaxableAmt,
            tblGSTR1HsnSacSummaryIntegratedTaxAmt, tblGSTR1HsnSacSummaryCentralTaxAmt,
            tblGSTR1HsnSacSummaryStateTaxAmount, tblGSTR1HsnSacSummaryTotalTaxAmt;
    @FXML
    private TextField tfGSTR1HsnSacSummarySearch;

    private ObservableList<GSTR1HsnSacSummaryDTO> originalData;

    @FXML
    private Button btExportPdf, btExportExcel, btExportCsv, btExportPrint;

    @FXML
    private BorderPane borderpane;

    @FXML
    private Label lblTotalTaxable_amt, lblTotalIgst_amt, lblTotalCgst_amt, lblTotalSgst_amt, lblTotalTax_amt, lblTotalInvoice_amt;

    String Start_date = "", End_date = "";

    @FXML
    private TextField tfStartDt, tfEndDt;

    private JsonObject jsonObject = null;

    public void initialize(URL url, ResourceBundle resourceBundle) {
        //TODO: resizing the table columns as per the resolution.
        tblvGSTR1HsnSacSummaryList.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        responsiveTableDesign();

        GSTR1HsnSacSummaryListLogger.info("Start of Initialize method of : GSTR1HsnSacSummaryController");
        Platform.runLater(() -> tfGSTR1HsnSacSummarySearch.requestFocus());
        fetchGSTR1HsnSacSummaryList("");
        borderpane.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
//                System.out.println("Esc Clicked....!");
                GlobalController.getInstance().addTabStatic(GSTR1_DASHBOARD_LIST_SLUG, false);
            }
        });

        tfEndDt.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
//                System.out.println("DateFilter From Date " + tfStartDt.getText().toString());
//                System.out.println("DateFilter To Date " + tfEndDt.getText().toString());
                fetchGSTR1HsnSacSummaryList("");
            }
        });

        DateValidator.applyDateFormat(tfStartDt);
        DateValidator.applyDateFormat(tfEndDt);

        tfGSTR1HsnSacSummarySearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filterData(newValue.trim());
        });

        nodetraversal(tfGSTR1HsnSacSummarySearch, tfStartDt);
        nodetraversal(tfStartDt, tfEndDt);
        nodetraversal(tfEndDt, btExportPdf);
        nodetraversal(btExportPdf, btExportExcel);
        nodetraversal(btExportExcel, btExportCsv);
        nodetraversal(btExportCsv, btExportPrint);
        tblvGSTR1HsnSacSummaryList.requestFocus();

        borderpane.addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent event) -> {
            if (event.getCode() == KeyCode.DOWN && tfGSTR1HsnSacSummarySearch.isFocused()){
                tblvGSTR1HsnSacSummaryList.getSelectionModel().select(0);
                tblvGSTR1HsnSacSummaryList.requestFocus();
            }
        });

        tblvGSTR1HsnSacSummaryList.setOnMouseClicked(event -> {
            if(event.getClickCount() == 2){
                GSTR1HsnSacSummaryDTO selectedItem = (GSTR1HsnSacSummaryDTO) tblvGSTR1HsnSacSummaryList.getSelectionModel().getSelectedItem();
                String id = selectedItem.getId();
                String prv_tax_rate = decimalFormat.format(Double.valueOf(selectedItem.getTax_rate()));
                GSTR1HsnSacSummaryController2.hsnReportId = Integer.valueOf(id);
                GSTR1HsnSacSummaryController2.prv_tax_rate = prv_tax_rate;
                System.out.println("prv_tax_rate----> " + prv_tax_rate);
                System.out.println("id------> " + id);
                GlobalController.getInstance().addTabStatic(GSTR1_HSN_SAC_SUMMARY_LIST2_SLUG, false);
            }
        });
        tblvGSTR1HsnSacSummaryList.addEventFilter(KeyEvent.ANY, (KeyEvent event) -> {
            if (event.getCode() == KeyCode.ENTER) {
                GSTR1HsnSacSummaryDTO selectedItem = (GSTR1HsnSacSummaryDTO) tblvGSTR1HsnSacSummaryList.getSelectionModel().getSelectedItem();
                String id = selectedItem.getId();
                String prv_tax_rate = decimalFormat.format(Double.valueOf(selectedItem.getTax_rate()));
                GSTR1HsnSacSummaryController2.hsnReportId = Integer.valueOf(id);
                GSTR1HsnSacSummaryController2.prv_tax_rate = prv_tax_rate;
                System.out.println("prv_tax_rate----> " + prv_tax_rate);
                System.out.println("id------> " + id);
                GlobalController.getInstance().addTabStatic(GSTR1_HSN_SAC_SUMMARY_LIST2_SLUG, false);
            }
        });
    }



    private void nodetraversal(Node current_node, Node next_node) {
        current_node.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                if ("tfEndDt".equals(current_node.getId())) {
                    getEndDate();
                }
                next_node.requestFocus();
                event.consume();
            }
            if (event.getCode() == KeyCode.TAB) {
                if ("tfEndDt".equals(current_node.getId())) {
                    getEndDate();
                }
            }

            if (event.getCode() == KeyCode.SPACE) {
                if (current_node instanceof Button button) {
                    button.fire();
                }
            }
        });
    }

    private void getEndDate() {
//        System.out.println("tfEndDt-------> " + tfEndDt.getText());
        fetchGSTR1HsnSacSummaryList("");
    }

    private void responsiveTableDesign() {
        tblGSTR1HsnSacSummarySrNo.prefWidthProperty().bind(tblvGSTR1HsnSacSummaryList.widthProperty().multiply(0.08));
        tblGSTR1HsnSacSummaryInvoiceNoHsnSac.prefWidthProperty().bind(tblvGSTR1HsnSacSummaryList.widthProperty().multiply(0.08));
        tblGSTR1HsnSacSummaryDescription.prefWidthProperty().bind(tblvGSTR1HsnSacSummaryList.widthProperty().multiply(0.12));
        tblGSTR1HsnSacSummaryTypeofSupply.prefWidthProperty().bind(tblvGSTR1HsnSacSummaryList.widthProperty().multiply(0.08));
        tblGSTR1HsnSacSummaryTotalQuantity.prefWidthProperty().bind(tblvGSTR1HsnSacSummaryList.widthProperty().multiply(0.08));
        tblGSTR1HsnSacSummaryTotalVolume.prefWidthProperty().bind(tblvGSTR1HsnSacSummaryList.widthProperty().multiply(0.08));
        tblGSTR1HsnSacSummaryTaxRate.prefWidthProperty().bind(tblvGSTR1HsnSacSummaryList.widthProperty().multiply(0.08));
        tblGSTR1HsnSacSummaryTaxableAmt.prefWidthProperty().bind(tblvGSTR1HsnSacSummaryList.widthProperty().multiply(0.08));
        tblGSTR1HsnSacSummaryIntegratedTaxAmt.prefWidthProperty().bind(tblvGSTR1HsnSacSummaryList.widthProperty().multiply(0.08));
        tblGSTR1HsnSacSummaryCentralTaxAmt.prefWidthProperty().bind(tblvGSTR1HsnSacSummaryList.widthProperty().multiply(0.08));
        tblGSTR1HsnSacSummaryStateTaxAmount.prefWidthProperty().bind(tblvGSTR1HsnSacSummaryList.widthProperty().multiply(0.08));
        tblGSTR1HsnSacSummaryTotalTaxAmt.prefWidthProperty().bind(tblvGSTR1HsnSacSummaryList.widthProperty().multiply(0.08));
    }

    private void fetchGSTR1HsnSacSummaryList(String searchKey) {
        GSTR1HsnSacSummaryListLogger.info("Fetch GSTR1 Hsn Sac Summary List  : GSTR1HsnSacSummaryController");

        APIClient apiClient = null;

        try {
            GSTR1HsnSacSummaryListLogger.debug("Get GSTR1 Hsn Sac Summary Data Started...");
            Map<String, String> body = new HashMap<>();
            body.put("searchText", "");
//            body.put("start_date", "");
//            body.put("end_date", "");
            if (!tfStartDt.getText().equalsIgnoreCase("") || !tfEndDt.getText().equalsIgnoreCase("")) {
//                System.out.println("date sdf " );
                body.put("start_date", Communicator.text_to_date.fromString(tfStartDt.getText()).toString());
                body.put("end_date", Communicator.text_to_date.fromString(tfEndDt.getText()).toString());
            }
//            System.out.println("body ----------->  " + body);
            String requestBody = Globals.mapToStringforFormData(body);
//            HttpResponse<String> response = APIClient.postFormDataRequest(requestBody, EndPoints.GSTR3B_OutwardTaxableSupplies_LIST);
            apiClient = new APIClient(EndPoints.GET_GSTR1_HSN_SAC_SUMMARY, requestBody, RequestType.FORM_DATA);
            apiClient.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
//                    JsonObject responseBody = new Gson().fromJson(response.body(), JsonObject.class);
                    jsonObject = new Gson().fromJson(workerStateEvent.getSource().getValue().toString(), JsonObject.class);
//            System.out.println("fetchGSTR1HsnSacSummaryList" + responseBody);
//                    System.out.println("jsonObject--------------------------" + jsonObject);
                    ObservableList<GSTR1HsnSacSummaryDTO> observableList = FXCollections.observableArrayList();
                    if (jsonObject.get("responseStatus").getAsInt() == 200) {
                        JsonArray responseList = jsonObject.getAsJsonArray("data");
//                System.out.println("fetchGSTR1HsnSacSummaryList" + responseList);
                        Start_date = (jsonObject.get("start_date").getAsString());
                        End_date = jsonObject.get("end_date").getAsString();
                        LocalDate startLocalDt = LocalDate.parse(Start_date);
                        LocalDate endLocalDt = LocalDate.parse(End_date);

                        if (responseList.size() > 0) {
                            tblvGSTR1HsnSacSummaryList.setVisible(true);
                            for (JsonElement element : responseList) {
                                JsonObject item = element.getAsJsonObject();
                                String id = item.get("hsn_id") != null ? item.get("hsn_id").getAsString() : "";
                                String hsn_sac = item.get("hsn_no") != null ? item.get("hsn_no").getAsString() : "";
                                String description = item.get("hsn_description") != null ? item.get("hsn_description").getAsString() : "";
                                String type_of_supply = item.get("supply_type") != null ? item.get("supply_type").getAsString() : "";
                                String total_quantity = item.get("Tqty") != null ? item.get("Tqty").getAsString() : "";
                                String total_volume = item.get("total_amount") != null ? item.get("total_amount").getAsString() : "";
                                String tax_rate = item.get("tax_rate") != null ? item.get("tax_rate").getAsString() : "";
                                String taxable_amt = item.get("taxable_amt") != null ? item.get("taxable_amt").getAsString() : "";
                                String igst_amt = item.get("igst_amt") != null ? item.get("igst_amt").getAsString() : "";
                                String cgst_amt = item.get("cgst_amt") != null ? item.get("cgst_amt").getAsString() : "";
                                String sgst_amt = item.get("sgst_amt") != null ? item.get("sgst_amt").getAsString() : "";
                                String total_tax_amt = item.get("tax_amt") != null ? item.get("tax_amt").getAsString() : "";

                                observableList.add(new GSTR1HsnSacSummaryDTO(id, hsn_sac, description, type_of_supply, total_quantity, total_volume, tax_rate, taxable_amt, igst_amt, cgst_amt, sgst_amt, total_tax_amt));
                            }

                            tfStartDt.setText(startLocalDt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                            tfEndDt.setText(endLocalDt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

                            tblGSTR1HsnSacSummaryInvoiceNoHsnSac.setCellValueFactory(new PropertyValueFactory<>("hsn_sac"));
                            tblGSTR1HsnSacSummaryDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
                            tblGSTR1HsnSacSummaryTypeofSupply.setCellValueFactory(new PropertyValueFactory<>("type_of_supply"));
                            tblGSTR1HsnSacSummaryTotalQuantity.setCellValueFactory(new PropertyValueFactory<>("total_quantity"));
                            tblGSTR1HsnSacSummaryTotalVolume.setCellValueFactory(new PropertyValueFactory<>("total_volume"));
                            tblGSTR1HsnSacSummaryTaxRate.setCellValueFactory(new PropertyValueFactory<>("tax_rate"));
                            tblGSTR1HsnSacSummaryTaxableAmt.setCellValueFactory(new PropertyValueFactory<>("taxable_amt"));
                            tblGSTR1HsnSacSummaryIntegratedTaxAmt.setCellValueFactory(new PropertyValueFactory<>("igst_amt"));
                            tblGSTR1HsnSacSummaryCentralTaxAmt.setCellValueFactory(new PropertyValueFactory<>("cgst_amt"));
                            tblGSTR1HsnSacSummaryStateTaxAmount.setCellValueFactory(new PropertyValueFactory<>("sgst_amt"));
                            tblGSTR1HsnSacSummaryTotalTaxAmt.setCellValueFactory(new PropertyValueFactory<>("total_tax_amt"));

                            tblvGSTR1HsnSacSummaryList.setItems(observableList);
                            originalData = observableList;
                            calculateTotalAmount();
                            GSTR1HsnSacSummaryListLogger.debug("Success in Displaying GSTR1 Hsn Sac Summary List : GSTR1HsnSacSummaryController");

                        } else {
                            GSTR1HsnSacSummaryListLogger.debug("GSTR1 Hsn Sac Summary List ResponseObject is null : GSTR1HsnSacSummaryController");
                            tblvGSTR1HsnSacSummaryList.getItems().clear();
                            calculateTotalAmount();
                        }
                    } else {
                        GSTR1HsnSacSummaryListLogger.debug("Error in response of GSTR1 Hsn Sac Summary List : GSTR1HsnSacSummaryController");
                        tblvGSTR1HsnSacSummaryList.getItems().clear();
                        calculateTotalAmount();
                    }
                }
            });
            apiClient.setOnCancelled(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    GSTR1HsnSacSummaryListLogger.error("Network API cancelled in fetchGSTR1HsnSacSummaryList()" + workerStateEvent.getSource().getValue().toString());

                }
            });

            apiClient.setOnFailed(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    GSTR1HsnSacSummaryListLogger.error("Network API failed in fetchGSTR1HsnSacSummaryList()" + workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.start();
            GSTR1HsnSacSummaryListLogger.debug("Get GSTR1 Hsn Sac Summary Data End...");

        } catch (Exception e) {
            GSTR1HsnSacSummaryListLogger.error("GSTR1 Hsn Sac Summary List Error is " + e.getMessage());
            e.printStackTrace();
        } finally {
            apiClient = null;
        }
    }

    private void filterData(String keyword) {
        ObservableList<GSTR1HsnSacSummaryDTO> filteredData = FXCollections.observableArrayList();

        GSTR1HsnSacSummaryListLogger.error("Search GSTR3B Outward Taxable Supplies in GSTR1CreditDebitRegisteredController");
        if (keyword.isEmpty()) {
            tblvGSTR1HsnSacSummaryList.setItems(originalData);
            return;
        }

        for (GSTR1HsnSacSummaryDTO item : originalData) {
            if (matchesKeyword(item, keyword)) {
                filteredData.add(item);
            }
        }

        tblvGSTR1HsnSacSummaryList.setItems(filteredData);
        calculateTotalAmount();
    }

    private void calculateTotalAmount() {
        ObservableList<GSTR1HsnSacSummaryDTO> filteredData = tblvGSTR1HsnSacSummaryList.getItems();
        // Calculate the Totals
        double tax_rate = 0.0;
        double taxable_amt = 0.0;
        double igst_amt = 0.0;
        double cgst_amt = 0.0;
        double sgst_amt = 0.0;
        double total_tax_amt = 0.0;
        for (GSTR1HsnSacSummaryDTO item : filteredData) {
            tax_rate += Double.parseDouble(item.getTax_rate().isEmpty() ? "0.0" : item.getTax_rate());
            taxable_amt += Double.parseDouble(item.getTaxable_amt().isEmpty() ? "0.0" : item.getTaxable_amt());
            igst_amt += Double.parseDouble(item.getIgst_amt().isEmpty() ? "0.0" : item.getIgst_amt());
            cgst_amt += Double.parseDouble(item.getCgst_amt().isEmpty() ? "0.0" : item.getCgst_amt());
            sgst_amt += Double.parseDouble(item.getSgst_amt().isEmpty() ? "0.0" : item.getSgst_amt());
            total_tax_amt += Double.parseDouble(item.getTotal_tax_amt().isEmpty() ? "0.0" : item.getTotal_tax_amt());
        }

        // Update Labels in the FXML
        lblTotalTaxable_amt.setText(String.format("%.2f", tax_rate));
        lblTotalIgst_amt.setText(String.format("%.2f", taxable_amt));
        lblTotalCgst_amt.setText(String.format("%.2f", igst_amt));
        lblTotalSgst_amt.setText(String.format("%.2f", cgst_amt));
        lblTotalTax_amt.setText(String.format("%.2f", sgst_amt));
        lblTotalInvoice_amt.setText(String.format("%.2f", total_tax_amt));
    }

    private boolean matchesKeyword(GSTR1HsnSacSummaryDTO item, String keyword) {
        String lowerCaseKeyword = keyword.toLowerCase();
        return
                item.getHsn_sac().toLowerCase().contains(lowerCaseKeyword) ||
                item.getDescription().toLowerCase().contains(lowerCaseKeyword) ||
                item.getType_of_supply().toLowerCase().contains(lowerCaseKeyword) ||
                item.getTotal_quantity().toLowerCase().contains(lowerCaseKeyword) ||
                item.getTotal_volume().toLowerCase().contains(lowerCaseKeyword) ||
                item.getTax_rate().toLowerCase().contains(lowerCaseKeyword) ||
                item.getTaxable_amt().toLowerCase().contains(lowerCaseKeyword) ||
                item.getIgst_amt().toLowerCase().contains(lowerCaseKeyword) ||
                item.getCgst_amt().toLowerCase().contains(lowerCaseKeyword) ||
                item.getSgst_amt().toLowerCase().contains(lowerCaseKeyword) ||
                item.getTotal_tax_amt().toLowerCase().contains(lowerCaseKeyword);
    }
}
