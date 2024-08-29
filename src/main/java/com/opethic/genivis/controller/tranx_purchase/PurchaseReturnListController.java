package com.opethic.genivis.controller.tranx_purchase;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.opethic.genivis.GenivisApplication;
import com.opethic.genivis.controller.commons.OverlaysEffect;
import com.opethic.genivis.dto.PurchaseChallanDTO;
import com.opethic.genivis.dto.PurchaseInvoiceDTO;
import com.opethic.genivis.dto.TranxLedgerWindowDTO;
import com.opethic.genivis.dto.pur_invoice.PurchaseInvoiceTable;
import com.opethic.genivis.dto.reqres.pur_tranx.TranxPurRtnListResDTO;
import com.opethic.genivis.dto.reqres.pur_tranx.TranxPurRtnRowDataListDTO;
import com.opethic.genivis.network.APIClient;
import com.opethic.genivis.network.EndPoints;
import com.opethic.genivis.network.RequestType;
import com.opethic.genivis.utils.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import static com.opethic.genivis.utils.FxmFileConstants.PURCHASE_RETURN_CREATE_SLUG;
import static com.opethic.genivis.utils.FxmFileConstants.PURCHASE_RETURN_EDIT_SLUG;

public class PurchaseReturnListController implements Initializable {
    @FXML
    private BorderPane bpPurReturnRootPane;
    @FXML
    private TableColumn<PurchaseInvoiceDTO, String> tblcPurRtnListNo;
    @FXML
    private TableColumn<PurchaseInvoiceDTO, String> tblcPurInvListTranxId;
    @FXML
    private TableColumn<PurchaseInvoiceDTO, String> tblcPurRtnListDate;
    @FXML
    private TableColumn<PurchaseInvoiceDTO, String> tblcPurRtnListSupplierName;
    @FXML
    private TableColumn<PurchaseInvoiceDTO, String> tblcPurRtnListNarration;
    @FXML
    private TableColumn<PurchaseInvoiceDTO, String> tblcPurRtnListTaxable;
    @FXML
    private TableColumn<PurchaseInvoiceDTO, String> tblcPurRtnListTax;
    @FXML
    private TableColumn<PurchaseInvoiceDTO, String> tblcPurRtnListBillAmount;
    @FXML
    private TableColumn<PurchaseInvoiceDTO, String> tblcPurRtnListPrint;
    @FXML
    private TableView<PurchaseInvoiceDTO> tblvPurRtnList;
    @FXML
    private Button btnPurRtnListCreate;
    private ObservableList<PurchaseInvoiceDTO> orgData;
    @FXML
    private TextField tfPurRtnListSearch, tfPurRtnListFromDt, tfPurRtnListToDt;
    @FXML
    private ScrollPane spRootPane;
    public static Integer purchaseReturnid = 0;

    public static String editedPurchaseRtnId = null; // ID for edited Sales Order
    public static final Logger purRtnListLogger = LoggerFactory.getLogger(PurchaseReturnListController.class);
    public static boolean isNewPurchaseChallanCreated = false; // Flag for new creation

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        ResponsiveWiseCssPicker();

        //todo : autofocus on TranxDate
        Platform.runLater(() -> tfPurRtnListSearch.requestFocus());
//         Enter traversal
        bpPurReturnRootPane.addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent event) -> {
            if (event.getCode() == KeyCode.ENTER) {
                KeyEvent newEvent = new KeyEvent(
                        null,
                        null,
                        KeyEvent.KEY_PRESSED,
                        "",
                        "\t",
                        KeyCode.TAB,
                        event.isShiftDown(),
                        event.isControlDown(),
                        event.isAltDown(),
                        event.isMetaDown()
                );

                Event.fireEvent(event.getTarget(), newEvent);
                event.consume();
            } else if (event.getCode() == KeyCode.N && event.isControlDown()) {
                btnPurRtnListCreate.fire();
            } else if (event.getCode() == KeyCode.E && event.isControlDown()) {
                purRtnEditPage();
            } else if (event.getCode() == KeyCode.D && event.isControlDown()) {
                handleCtrlDPressed();
            } else if ((event.getCode().isModifierKey() || event.getCode().isLetterKey() ||
                    event.getCode().isDigitKey() || event.getCode() == KeyCode.BACK_SPACE)
                    && tblvPurRtnList.isFocused()) {
                tfPurRtnListSearch.requestFocus();
            } else if (event.getCode() == KeyCode.DOWN && tfPurRtnListSearch.isFocused()) {
                tblvPurRtnList.getSelectionModel().select(0);
                tfPurRtnListSearch.requestFocus();
            }
        });


        //code to write when the window opens
        tblvPurRtnList.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        getPurchaseInvoiceList("");
        orgData = tblvPurRtnList.getItems();
        tfPurRtnListSearch.setOnKeyTyped(event -> {
            String search = tfPurRtnListSearch.getText().trim();
            handleSearch(search);
        });
        purRtnListLogger.debug("Before Double click event to get the id from selected row of product list table in " +
                "tvProductList.setRowFactory() ");

        tblvPurRtnList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                purRtnEditPage();
            }
        });
        btnPurRtnListCreate.setOnAction(event -> GlobalController.getInstance().addTabStatic(PURCHASE_RETURN_CREATE_SLUG, false));
        // Add a key event listener to the text field

    }

    private void ResponsiveWiseCssPicker() {
        double height = Screen.getPrimary().getBounds().getHeight();
        double width = Screen.getPrimary().getBounds().getWidth();
        System.out.println("width" + width);
        if (width >= 992 && width <= 1024) {
            bpPurReturnRootPane.getStylesheets().add(GenivisApplication.class.getResource("ui/css/TranxCommonCssStyles/tranxCommonCssStyle1.css").toExternalForm());
        } else if (width >= 1025 && width <= 1280) {
            bpPurReturnRootPane.getStylesheets().add(GenivisApplication.class.getResource("ui/css/TranxCommonCssStyles/tranxCommonCssStyle2.css").toExternalForm());
        } else if (width >= 1281 && width <= 1368) {
            bpPurReturnRootPane.getStylesheets().add(GenivisApplication.class.getResource("ui/css/TranxCommonCssStyles/tranxCommonCssStyle3.css").toExternalForm());
        } else if (width >= 1369 && width <= 1400) {
            bpPurReturnRootPane.getStylesheets().add(GenivisApplication.class.getResource("ui/css/TranxCommonCssStyles/tranxCommonCssStyle4.css").toExternalForm());
        } else if (width >= 1401 && width <= 1440) {
            bpPurReturnRootPane.getStylesheets().add(GenivisApplication.class.getResource("ui/css/TranxCommonCssStyles/tranxCommonCssStyle5.css").toExternalForm());
        } else if (width >= 1441 && width <= 1680) {
            bpPurReturnRootPane.getStylesheets().add(GenivisApplication.class.getResource("ui/css/TranxCommonCssStyles/tranxCommonCssStyle6.css").toExternalForm());
        } else if (width >= 1681 && width <= 1920) {
            bpPurReturnRootPane.getStylesheets().add(GenivisApplication.class.getResource("ui/css/TranxCommonCssStyles/tranxCommonCssStyle7.css").toExternalForm());
        }
    }

    private void handleCtrlDPressed() {
        try {
            PurchaseInvoiceDTO selectedItem = (PurchaseInvoiceDTO) tblvPurRtnList.getSelectionModel().getSelectedItem();
            purchaseReturnid = Integer.valueOf(selectedItem.getPurInvoiceId());
            if (purchaseReturnid != null) {
                Integer id = purchaseReturnid;
                System.out.println("ctrl+D" + id);
                deleteAPICall(purchaseReturnid);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteAPICall(Integer id) {
        AlertUtility.CustomCallback callback = number -> {
            if (number == 1) {
                APIClient apiClient = null;
                try {
                    purRtnListLogger.debug("Get deleteAPICall Started...");
                    Map<String, String> map = new HashMap<>();
                    map.put("id", String.valueOf(id));
                    String formData = Globals.mapToStringforFormData(map);
                    apiClient = new APIClient(EndPoints.DEL_PUR_RETURN, formData, RequestType.FORM_DATA);
                    apiClient.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                        @Override
                        public void handle(WorkerStateEvent workerStateEvent) {
                            JsonObject jsonObject = new Gson().fromJson(workerStateEvent.getSource().getValue().toString(), JsonObject.class);
                            String message = jsonObject.get("message").getAsString();
                            if (jsonObject.get("responseStatus").getAsInt() == 200) {
                                AlertUtility.AlertSuccessTimeout(AlertUtility.alertTypeSuccess, message, input -> {
                                    getPurchaseInvoiceList("");
                                    tfPurRtnListSearch.requestFocus();
                                });
                            } else {
                                AlertUtility.AlertErrorTimeout(AlertUtility.alertTypeError, "Failed to Delete Purchase Challan.", in -> {
                                    tfPurRtnListSearch.requestFocus();
                                });
                            }
                        }
                    });
                    apiClient.setOnCancelled(new EventHandler<WorkerStateEvent>() {
                        @Override
                        public void handle(WorkerStateEvent workerStateEvent) {
                            purRtnListLogger.error("Network API cancelled in deleteAPICall()" +
                                    workerStateEvent.getSource().getValue().toString());

                        }
                    });

                    apiClient.setOnFailed(new EventHandler<WorkerStateEvent>() {
                        @Override
                        public void handle(WorkerStateEvent workerStateEvent) {
                            purRtnListLogger.error("Network API cancelled in deleteAPICall()" +
                                    workerStateEvent.getSource().getValue().toString());

                        }
                    });
                    apiClient.start();
                    purRtnListLogger.debug("Get deleteAPICall end");

                } catch (Exception e) {
                    e.printStackTrace();
                    purRtnListLogger.error("Exception deleteAPICall() : " + Globals.getExceptionString(e));
                } finally {
                    apiClient = null;
                }
            } else {
                System.out.println("working!");
            }
        };
        AlertUtility.AlertConfirmation(AlertUtility.alertTypeConfirmation, "Do you want to Delete", callback);
    }

    //function to GET the list of purchase invoice
    public void getPurchaseInvoiceList(String key) {
        ObservableList<PurchaseInvoiceDTO> observableList = FXCollections.observableArrayList();
        try {
            Map<String, String> map = new HashMap<>();
            Map<String, Object> sortObject = new HashMap<>();
            sortObject.put("colId", null);
            sortObject.put("isAsc", true);
            map.put("endDate", "");
            map.put("pageNo", "1");
            map.put("pageSize", "100");
            map.put("searchText", key);
            map.put("sort", sortObject.toString());
            map.put("startDate", tfPurRtnListFromDt.getText());
            map.put("endDate", tfPurRtnListToDt.getText());
            String formData = Globals.mapToString(map);
            HttpResponse<String> response = APIClient.postJsonRequest(formData, EndPoints.GET_PUR_RETURN_LIST);
            TranxPurRtnListResDTO responseBody = new Gson().fromJson(response.body(), TranxPurRtnListResDTO.class);
            if (responseBody.getResponseStatus() == 200) {
                List<TranxPurRtnRowDataListDTO> list = responseBody.getResponseObject().getData();
                if (list.size() > 0) {
                    for (TranxPurRtnRowDataListDTO element : list) {
                        String id = "" + element.getId();
                        String purInvoiceNumber = "" + element.getPurReturnNo();
                        String tranxId = element.getTranxCode();
                        String invoiceDate = DateConvertUtil.convertDispDateFormat(element.getPurchaseReturnDate());
                        String supplierName = element.getSundryCreditorName();
                        String narration = element.getNarration();
                        String taxable = element.getTaxableAmt() != null ? element.getTaxableAmt().toString() : "";
                        String tax = element.getTaxAmt() != null ? element.getTaxAmt().toString() : "";
                        String billAmount = String.valueOf(element.getTotalAmount());
                        observableList.add(new PurchaseInvoiceDTO(id, purInvoiceNumber, tranxId, invoiceDate,
                                supplierName, narration, taxable, tax, billAmount));
                    }
                    tblcPurRtnListNo.setCellValueFactory(new PropertyValueFactory<>("purInvoiceNumber"));
                    tblcPurInvListTranxId.setCellValueFactory(new PropertyValueFactory<>("purInvoiceTranxId"));
                    tblcPurRtnListDate.setCellValueFactory(new PropertyValueFactory<>("purInvoiceDate"));
                    tblcPurRtnListSupplierName.setCellValueFactory(new PropertyValueFactory<>("purInvoiceSupplierName"));
                    tblcPurRtnListNarration.setCellValueFactory(new PropertyValueFactory<>("purInvoiceNarration"));
                    tblcPurRtnListTaxable.setCellValueFactory(new PropertyValueFactory<>("purInvoiceTaxable"));
                    tblcPurRtnListTax.setCellValueFactory(new PropertyValueFactory<>("purInvoiceTax"));
                    tblcPurRtnListBillAmount.setCellValueFactory(new PropertyValueFactory<>("purInvoiceBillAmount"));
                    tblvPurRtnList.setItems(observableList);
                    if (isNewPurchaseChallanCreated) {
                        tblvPurRtnList.scrollTo(0);
                        isNewPurchaseChallanCreated = false; // Reset the flag
                    } else if (editedPurchaseRtnId != null) {
                        for (PurchaseInvoiceDTO purRtn : observableList) {
                            if (purRtn.getPurInvoiceId().equals(editedPurchaseRtnId)) {
                                tblvPurRtnList.getSelectionModel().select(purRtn);
                                tblvPurRtnList.scrollTo(purRtn);
                                editedPurchaseRtnId = null; // Reset the ID
                                break;
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();

        }


    }

    public void handleSearch(String search) {   //for search in list
        getPurchaseInvoiceList(search);
    }

    public boolean matchesKeyword(PurchaseInvoiceDTO dtoItems, String search) {
        String lowerCase = search.toLowerCase();

        return dtoItems.getPurInvoiceDate().toLowerCase().contains(lowerCase) ||
                dtoItems.getPurInvoiceSupplierName().toLowerCase().contains(lowerCase) ||
                dtoItems.getPurInvoiceBillAmount().toLowerCase().contains(lowerCase);
    }

    private void purRtnEditPage() {
        try {
            PurchaseInvoiceDTO returnDto = tblvPurRtnList.getSelectionModel().getSelectedItem();
            purchaseReturnid = Integer.parseInt(returnDto.getPurInvoiceId());
            editedPurchaseRtnId = String.valueOf(editedPurchaseRtnId);
            GlobalController.getInstance().addTabStatic(FxmFileConstants.PURCHASE_RETURN_EDIT_SLUG, false);
        } catch (Exception e) {

        }

    }

    public static Integer getPurRtnInvoiceId() {
        return purchaseReturnid;
    }
}
