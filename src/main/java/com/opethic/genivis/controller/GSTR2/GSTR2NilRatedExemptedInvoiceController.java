package com.opethic.genivis.controller.GSTR2;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.opethic.genivis.dto.GSTR2.GSTR2CrDrRegisterDTO;
import com.opethic.genivis.dto.GSTR2.GSTR2NilRatedExemptedInvoiceDTO;
import com.opethic.genivis.dto.GSTR3BAllOtherITCDTO;
import com.opethic.genivis.dto.reqres.product.Communicator;
import com.opethic.genivis.network.APIClient;
import com.opethic.genivis.network.EndPoints;
import com.opethic.genivis.network.RequestType;
import com.opethic.genivis.utils.CommonTraversa;
import com.opethic.genivis.utils.DateValidator;
import com.opethic.genivis.utils.GlobalController;
import com.opethic.genivis.utils.Globals;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import static com.opethic.genivis.utils.FxmFileConstants.GSTR2_DASHBOARD_LIST_SLUG;

public class GSTR2NilRatedExemptedInvoiceController implements Initializable {
    private static final Logger GSTR2NilRatedExemptedInvoiceLogger = LoggerFactory.getLogger(GSTR2NilRatedExemptedInvoiceController.class);

    @FXML
    private TextField tfGSTR2NilRatedExemptedInvoiceSearch, tfStartDt, tfEndDt;
    @FXML
    private TableView<GSTR2NilRatedExemptedInvoiceDTO> tblvNilRatedExemptedInvoiceList;
    @FXML
    private Button btExportPdf, btExportExcel, btExportCsv, btExportPrint;
    @FXML
    private Label lblTotalNilRated_amt, lblTotalExempted_amt;
    @FXML
    private TableColumn<GSTR2NilRatedExemptedInvoiceDTO, String> tblcNilRatedExemptedInvoiceSrNo, tblcNilRatedExemptedInvoiceVoucherType,
            tblcNilRatedExemptedInvoiceParticulars, tblcNilRatedExemptedInvoiceNilRated, tblcNilRatedExemptedInvoiceExempted;
    private ObservableList<GSTR2NilRatedExemptedInvoiceDTO> originalData;
    @FXML
    private BorderPane mainBorderpane;

    private JsonObject jsonObject = null;

    String Start_date = "", End_date = "";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        GSTR2NilRatedExemptedInvoiceLogger.info("Start of Initialize method of : GSTR2NilRatedExemptedInvoiceController");

        //TODO: resizing the table columns as per the resolution.
        tblvNilRatedExemptedInvoiceList.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        responsiveTableDesign();

        Platform.runLater(() -> tfGSTR2NilRatedExemptedInvoiceSearch.requestFocus());

        mainBorderpane.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
//                System.out.println("Esc Clicked....!");
                GlobalController.getInstance().addTabStatic(GSTR2_DASHBOARD_LIST_SLUG, false);
            }
        });

        GSTR2NilRatedExemptedInvoiceList("");

        tfEndDt.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
//                System.out.println("DateFilter From Date " + tfStartDt.getText().toString());
//                System.out.println("DateFilter To Date " + tfEndDt.getText().toString());
                GSTR2NilRatedExemptedInvoiceList("");
            }
        });

        DateValidator.applyDateFormat(tfStartDt);
        DateValidator.applyDateFormat(tfEndDt);

        tfGSTR2NilRatedExemptedInvoiceSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filterData(newValue.trim());
        });

        // Setting the handler for each node
        setKeyPressedHandler(tfGSTR2NilRatedExemptedInvoiceSearch);
        setKeyPressedHandler(tfStartDt);
        setKeyPressedHandler(tfEndDt);
        setKeyPressedHandler(btExportPdf);
        setKeyPressedHandler(btExportExcel);
        setKeyPressedHandler(btExportCsv);
        setKeyPressedHandler(btExportPrint);
        tblvNilRatedExemptedInvoiceList.requestFocus();

        tfEndDt.setOnKeyPressed(event ->{
            if(event.getCode() == KeyCode.ENTER){
                GSTR2NilRatedExemptedInvoiceList("");
                Node nextNode = CommonTraversa.getNextFocusableNode(btExportPdf.getScene());
                if (nextNode != null) {
                    nextNode.requestFocus();
                }
            }
            if(event.getCode() == KeyCode.TAB){
                GSTR2NilRatedExemptedInvoiceList(""); 
            }
        });
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

    private void responsiveTableDesign() {
        tblcNilRatedExemptedInvoiceSrNo.prefWidthProperty().bind(tblvNilRatedExemptedInvoiceList.widthProperty().multiply(0.10));
        tblcNilRatedExemptedInvoiceVoucherType.prefWidthProperty().bind(tblvNilRatedExemptedInvoiceList.widthProperty().multiply(0.20));
        tblcNilRatedExemptedInvoiceParticulars.prefWidthProperty().bind(tblvNilRatedExemptedInvoiceList.widthProperty().multiply(0.50));
        tblcNilRatedExemptedInvoiceNilRated.prefWidthProperty().bind(tblvNilRatedExemptedInvoiceList.widthProperty().multiply(0.10));
        tblcNilRatedExemptedInvoiceExempted.prefWidthProperty().bind(tblvNilRatedExemptedInvoiceList.widthProperty().multiply(0.10));
    }

    private void GSTR2NilRatedExemptedInvoiceList(String searchKey) {
        GSTR2NilRatedExemptedInvoiceLogger.info("Fetch GSTR2 Nil Rated Exempted Invoice List  : GSTR2NilRatedExemptedInvoiceController");
        APIClient apiClient = null;
        try {
//            System.out.println("tryyyyyy.....");
            GSTR2NilRatedExemptedInvoiceLogger.debug("Get GSTR2 Nil Rated Exempted Invoice Data Started...");
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
            apiClient = new APIClient(EndPoints.GSTR2_Nil_Rated_Exempted, requestBody, RequestType.FORM_DATA);
            apiClient.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    jsonObject = new Gson().fromJson(workerStateEvent.getSource().getValue().toString(), JsonObject.class);
                    ObservableList<GSTR2NilRatedExemptedInvoiceDTO> observableList = FXCollections.observableArrayList();
                    String nil_rated = "", exempted = "";
                    if (jsonObject.get("responseStatus").getAsInt() == 200) {
                        JsonArray responseList = jsonObject.getAsJsonArray("data");
                        System.out.println("GSTR2NilRatedExemptedInvoiceList---> " + responseList);
                        Start_date = (jsonObject.get("start_date").getAsString());
                        End_date = jsonObject.get("end_date").getAsString();
                        LocalDate startLocalDt = LocalDate.parse(Start_date);
                        LocalDate endLocalDt = LocalDate.parse(End_date);
                        int countSr = 1;
                        if (responseList.size() > 0) {
                            tblvNilRatedExemptedInvoiceList.setVisible(true);
                            for (JsonElement element : responseList) {
                                JsonObject item = element.getAsJsonObject();
                                String id = item.get("id") != null ? item.get("id").getAsString() : "";
                                String voucher_type = item.get("tax_type") != null ? item.get("tax_type").getAsString() : "";
                                String particulars = item.get("ledger_name") != null ? item.get("ledger_name").getAsString() : "";
                                if (voucher_type.equals("Nilrated")){
//                                    System.out.println("<--------nilrated------>" + voucher_type);
                                    nil_rated = item.get("total_amount") != null ? item.get("total_amount").getAsString() : "";
                                } else if (voucher_type.equals("Exempted")){
//                                    System.out.println("<--------exempted------>" + voucher_type);
                                    exempted = item.get("total_amount") != null ? item.get("total_amount").getAsString() : "";
                                }

                                observableList.add(new GSTR2NilRatedExemptedInvoiceDTO(String.valueOf(countSr++), id, voucher_type, particulars, nil_rated, exempted));
                            }

                            tfStartDt.setText(startLocalDt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                            tfEndDt.setText(endLocalDt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

                            tblcNilRatedExemptedInvoiceSrNo.setCellValueFactory(new PropertyValueFactory<>("sr_no"));
                            tblcNilRatedExemptedInvoiceVoucherType.setCellValueFactory(new PropertyValueFactory<>("voucher_type"));
                            tblcNilRatedExemptedInvoiceParticulars.setCellValueFactory(new PropertyValueFactory<>("particulars"));
                            tblcNilRatedExemptedInvoiceNilRated.setCellValueFactory(new PropertyValueFactory<>("nil_rated"));
                            tblcNilRatedExemptedInvoiceExempted.setCellValueFactory(new PropertyValueFactory<>("exempted"));


                            tblvNilRatedExemptedInvoiceList.setItems(observableList);

                            originalData = observableList;
                            calculateTotalAmount();
                            GSTR2NilRatedExemptedInvoiceLogger.debug("Success in Displaying GSTR2 Nil Rated Exempted Invoice List : GSTR2NilRatedExemptedInvoiceController");

                        } else {
                            GSTR2NilRatedExemptedInvoiceLogger.debug("GSTR2 Nil Rated Exempted Invoice List ResponseObject is null : GSTR2NilRatedExemptedInvoiceController");
                            tblvNilRatedExemptedInvoiceList.getItems().clear();
                            calculateTotalAmount();
                        }
                    } else {
                        GSTR2NilRatedExemptedInvoiceLogger.debug("Error in response of GSTR2 Nil Rated Exempted Invoice List : GSTR2NilRatedExemptedInvoiceController");
                        tblvNilRatedExemptedInvoiceList.getItems().clear();
                        calculateTotalAmount();
                    }
                }
            });
            apiClient.setOnCancelled(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    GSTR2NilRatedExemptedInvoiceLogger.error("Network API cancelled in GSTR2NilRatedExemptedInvoiceList()" + workerStateEvent.getSource().getValue().toString());

                }
            });

            apiClient.setOnFailed(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    GSTR2NilRatedExemptedInvoiceLogger.error("Network API failed in GSTR2NilRatedExemptedInvoiceList()" + workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.start();
            GSTR2NilRatedExemptedInvoiceLogger.debug("Get GSTR2 Nil Rated Exempted Invoice Data End...");

        } catch (Exception e) {
            GSTR2NilRatedExemptedInvoiceLogger.error("GSTR2 Nil Rated Exempted Invoice List Error is " + e.getMessage());
            e.printStackTrace();
        } finally {
            apiClient = null;
        }
    }

    private void filterData(String keyword) {
        ObservableList<GSTR2NilRatedExemptedInvoiceDTO> filteredData = FXCollections.observableArrayList();

        GSTR2NilRatedExemptedInvoiceLogger.error("Search GSTR2 Nil Rated Exempted Invoice in GSTR2NilRatedExemptedInvoiceController");
        if (keyword.isEmpty()) {
            tblvNilRatedExemptedInvoiceList.setItems(originalData);
            return;
        }

        for (GSTR2NilRatedExemptedInvoiceDTO item : originalData) {
            if (matchesKeyword(item, keyword)) {
                filteredData.add(item);
            }
        }

        tblvNilRatedExemptedInvoiceList.setItems(filteredData);
        calculateTotalAmount();
    }

    private boolean matchesKeyword(GSTR2NilRatedExemptedInvoiceDTO item, String keyword) {
        String lowerCaseKeyword = keyword.toLowerCase();
        return
                item.getVoucher_type().toLowerCase().contains(lowerCaseKeyword) ||
                item.getParticulars().toLowerCase().contains(lowerCaseKeyword) ||
                item.getParticulars().toLowerCase().contains(lowerCaseKeyword) ||
                item.getNil_rated().toLowerCase().contains(lowerCaseKeyword) ||
                item.getExempted().toLowerCase().contains(lowerCaseKeyword);
    }

    private void calculateTotalAmount() {
        ObservableList<GSTR2NilRatedExemptedInvoiceDTO> filteredData = tblvNilRatedExemptedInvoiceList.getItems();
        // Calculate the Totals
        double totalNilRated_amt = 0.0;
        double totalExempted_amt = 0.0;

        for (GSTR2NilRatedExemptedInvoiceDTO item : filteredData) {
            totalNilRated_amt += Double.parseDouble(item.getNil_rated().isEmpty() ? "0.0" : item.getNil_rated());
            totalExempted_amt += Double.parseDouble(item.getExempted().isEmpty() ? "0.0" : item.getExempted());
        }

        // Update Labels in the FXML
        lblTotalNilRated_amt.setText(String.format("%.2f", totalNilRated_amt));
        lblTotalExempted_amt.setText(String.format("%.2f", totalExempted_amt));
    }
}
