// Controller

package com.opethic.genivis.controller.franchise_dashboard;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.opethic.genivis.controller.dashboard.DashboardController;
import com.opethic.genivis.network.APIClient;
import com.opethic.genivis.network.EndPoints;
import com.opethic.genivis.network.RequestType;
import com.opethic.genivis.utils.AlertUtility;
import com.opethic.genivis.utils.Globals;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class FranchiseDashboard implements Initializable {

    private static final Logger logger = LogManager.getLogger(FranchiseDashboard.class);
    private JsonObject jsonObject = null;
    @FXML
    private ImageView imageView, imageView1,imageView2;
    private int currentIndex = 0;
    private int currentIndex1 = 0;
    @FXML
    private Button prevButton;
    @FXML
    private StackPane firstStack, secondStack;
    @FXML
    private Button prevButton1;
    @FXML
    private Button nextButton;
    @FXML
    private Button nextButton1;
    @FXML

    private Image[] images;
    private Image[] images1;

    @FXML
    private TableView tblvFrDashboardTopFr;
    @FXML
    private TableColumn tblcFrDashboardSrNo, tblcFrDashboardFrName;

    @FXML
    private TableView tblvFrDashboardCashSummary;
    @FXML
    private TableColumn tblcFrDashboardLedgerName, tblcFrDashboardAmount, tblcFrDashboardDays;

    @FXML
    private TableView tblvFrDashboardProducts;
    @FXML
    private TableColumn tblcFrDashboardNumber, tblcFrDashboardProductsName;

    @FXML
    private TableView tblvFrDashboardTranx;
    @FXML
    private TableColumn tblcFrDashboardTranxSrNo, tblcFrDashboardTranxAddAndName, tblcFrDashboardTranxAmt;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        //TODO: resizing the table columns as per the resolution.
        tblvFrDashboardTopFr.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tblvFrDashboardCashSummary.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tblvFrDashboardProducts.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tblvFrDashboardTranx.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

//        tblcFrDashboardSrNo.prefWidthProperty().bind(tblvFrDashboardTopFr.widthProperty().multiply(0.2));
//        tblcFrDashboardFrName.prefWidthProperty().bind(tblvFrDashboardTopFr.widthProperty().multiply(0.8));
//
//        tblcFrDashboardLedgerName.prefWidthProperty().bind(tblvFrDashboardTopFr.widthProperty().multiply(0.6));
//        tblcFrDashboardAmount.prefWidthProperty().bind(tblvFrDashboardTopFr.widthProperty().multiply(0.2));
//        tblcFrDashboardDays.prefWidthProperty().bind(tblvFrDashboardTopFr.widthProperty().multiply(0.2));
//
//        tblcFrDashboardNumber.prefWidthProperty().bind(tblvFrDashboardTopFr.widthProperty().multiply(0.2));
//        tblcFrDashboardProductsName.prefWidthProperty().bind(tblvFrDashboardTopFr.widthProperty().multiply(0.8));

//        tblcFrDashboardTranxSrNo.prefWidthProperty().bind(tblvFrDashboardTranx.widthProperty().multiply(0.2));
//        tblcFrDashboardTranxAddAndName.prefWidthProperty().bind(tblvFrDashboardTranx.widthProperty().multiply(0.6));
//        tblcFrDashboardTranxAmt.prefWidthProperty().bind(tblvFrDashboardTranx.widthProperty().multiply(0.2));

        System.out.println("current Index initial -->" + currentIndex);


        images = new Image[]{
                new Image(getClass().getResourceAsStream("/com/opethic/genivis/ui/assets/franchise_dashboard/banner1.png")),
                new Image(getClass().getResourceAsStream("/com/opethic/genivis/ui/assets/franchise_dashboard/banner2.png")),
                new Image(getClass().getResourceAsStream("/com/opethic/genivis/ui/assets/franchise_dashboard/banner1.png"))
        };
        images1 = new Image[]{
                new Image(getClass().getResourceAsStream("/com/opethic/genivis/ui/assets/franchise_dashboard/banner1.png")),
                new Image(getClass().getResourceAsStream("/com/opethic/genivis/ui/assets/franchise_dashboard/banner2.png")),
                new Image(getClass().getResourceAsStream("/com/opethic/genivis/ui/assets/franchise_dashboard/banner1.png"))
        };


        // Adjust ImageView properties
        imageView1.setPreserveRatio(false);
        imageView1.fitWidthProperty().bind(secondStack.widthProperty());
        imageView1.fitHeightProperty().bind(secondStack.heightProperty());

        // Adjust ImageView properties
        imageView.setPreserveRatio(false);
        imageView.fitWidthProperty().bind(firstStack.widthProperty());
        imageView.fitHeightProperty().bind(firstStack.heightProperty());

        // Set the first image as selected
//        nextImage();
//        nextImage1();

        //TODO: sale invoice
        getSaleInvoiceList("");

    }

    @FXML
    public void prevImage() {
        System.out.println("currentIndex in prev " + currentIndex);
        currentIndex = (currentIndex - 1 + images.length) % images.length;
        showImage(currentIndex);
    }

    @FXML
    public void nextImage() {
        System.out.println("currentIndex in next " + currentIndex);
        currentIndex = (currentIndex + 1) % images.length;
//        System.out.println("currentIndex in next after "+currentIndex);
        showImage(currentIndex);
    }

    private void showImage(int index) {
        imageView.setImage(images[index]);
    }

    @FXML
    public void prevImage1() {
        System.out.println("currentIndex in prev " + currentIndex1);
        currentIndex1 = (currentIndex1 - 1 + images1.length) % images1.length;
        showImage1(currentIndex1);
    }

    @FXML
    public void nextImage1() {
        System.out.println("currentIndex in next " + currentIndex1);
        currentIndex1 = (currentIndex1 + 1) % images1.length;
//        System.out.println("currentIndex in next after "+currentIndex);
        showImage1(currentIndex1);
    }

    private void showImage1(int index) {
        imageView1.setImage(images1[index]);
    }

    public void getSaleInvoiceList(String searchKey) {
        APIClient apiClient = null;
        try {
            logger.debug("Get sale invoice Data Started...");
            Map<String, String> body = new HashMap<>();
            body.put("pageNo", "1");
            body.put("pageSize", "50");
            body.put("searchText", searchKey);
            body.put("sort", "{ \\\"colId\\\": null, \\\"isAsc\\\": true }");
            String requestBody = Globals.mapToString(body);

            apiClient = new APIClient(EndPoints.GET_SALE_INVOICE_LIST, requestBody, RequestType.JSON);
            apiClient.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    jsonObject = new Gson().fromJson(workerStateEvent.getSource().getValue().toString(), JsonObject.class);
                    System.out.println("jsonObject---<>"+jsonObject);
                    if (jsonObject.get("responseStatus").getAsInt() == 200) {
                        JsonArray responseArray = jsonObject.get("data").getAsJsonArray();
                        System.out.println("responseObject sale invoice-->"+responseArray);
                    } else {
                        //TODO : use logger for alert messages
                        AlertUtility.AlertDialogForError("WARNING", "Failed To Load Data", input -> {
                            if (input) {
                            }
                        });
                    }
                }
            });
            apiClient.setOnCancelled(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    logger.error("Network API cancelled in getSaleInvoiceList()" + workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.setOnFailed(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    logger.error("Network API failed in getSaleInvoiceList()" + workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.start();
        } catch (Exception e) {
            e.printStackTrace();
            //TODO : use logger for alert messages
            AlertUtility.AlertDialogForError("WARNING", "Failed to Connect Server", input -> {
                if (input) {
                }
            });
        } finally {
            apiClient = null;
        }
    }

}
