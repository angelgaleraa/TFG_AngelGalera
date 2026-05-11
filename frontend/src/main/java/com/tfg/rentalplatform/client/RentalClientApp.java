package com.tfg.rentalplatform.client;

import com.tfg.rentalplatform.client.api.ApiClient;
import com.tfg.rentalplatform.client.controller.AdminOverviewController;
import com.tfg.rentalplatform.client.controller.CatalogController;
import com.tfg.rentalplatform.client.controller.ChatController;
import com.tfg.rentalplatform.client.controller.MyItemsController;
import com.tfg.rentalplatform.client.controller.NotificationController;
import com.tfg.rentalplatform.client.controller.PaymentController;
import com.tfg.rentalplatform.client.controller.ReservationController;
import com.tfg.rentalplatform.client.controller.ReportController;
import com.tfg.rentalplatform.client.controller.ReviewController;
import com.tfg.rentalplatform.client.controller.SupportChatController;
import com.tfg.rentalplatform.client.mapper.ItemMapper;
import com.tfg.rentalplatform.client.model.ItemRow;
import com.tfg.rentalplatform.client.realtime.RealtimeChatClient;
import com.tfg.rentalplatform.client.ui.ActivityRowFactory;
import com.tfg.rentalplatform.client.ui.AuthViewFactory;
import com.tfg.rentalplatform.client.ui.CatalogDetailModalFactory;
import com.tfg.rentalplatform.client.ui.CatalogViewFactory;
import com.tfg.rentalplatform.client.ui.FormControlHelper;
import com.tfg.rentalplatform.client.ui.GeoComboFactory;
import com.tfg.rentalplatform.client.ui.ItemCardFactory;
import com.tfg.rentalplatform.client.ui.ItemFormHelper;
import com.tfg.rentalplatform.client.ui.ModalFactory;
import com.tfg.rentalplatform.client.ui.MyItemsViewFactory;
import com.tfg.rentalplatform.client.ui.NotificationsViewFactory;
import com.tfg.rentalplatform.client.ui.ProfileViewFactory;
import com.tfg.rentalplatform.client.ui.PublicProfileModalFactory;
import com.tfg.rentalplatform.client.ui.ResponsiveCardGrid;
import com.tfg.rentalplatform.client.ui.ReservationDetailFactory;
import com.tfg.rentalplatform.client.ui.ReservationsViewFactory;
import com.tfg.rentalplatform.client.ui.ReportModalFactory;
import com.tfg.rentalplatform.client.ui.ReviewUiFactory;
import com.tfg.rentalplatform.client.ui.UiFactory;
import com.tfg.rentalplatform.client.util.BackendLauncher;
import com.tfg.rentalplatform.client.util.BookingSummary;
import com.tfg.rentalplatform.client.util.CatalogOptions;
import com.tfg.rentalplatform.client.util.GeoDataService;
import com.tfg.rentalplatform.client.util.ReservationText;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.animation.FadeTransition;
import javafx.animation.Animation;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.input.KeyCode;

import java.io.File;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.tfg.rentalplatform.client.util.LocationUtils.normalizeSearchText;
import static com.tfg.rentalplatform.client.util.FormatUtils.formatReservationDates;
import static com.tfg.rentalplatform.client.util.FormatUtils.toInt;
import static com.tfg.rentalplatform.client.util.FormatUtils.toLong;
import static com.tfg.rentalplatform.client.util.FeedbackText.loginMessage;
import static com.tfg.rentalplatform.client.util.FeedbackText.registerMessage;
import static com.tfg.rentalplatform.client.util.FeedbackText.shouldShowUserFeedback;
import static com.tfg.rentalplatform.client.util.FeedbackText.userFeedbackMessage;
import static com.tfg.rentalplatform.client.util.TextUtils.cleanDisplayText;
import static com.tfg.rentalplatform.client.util.TextUtils.nullableString;
import static com.tfg.rentalplatform.client.util.CatalogOptions.displayCategory;
import static com.tfg.rentalplatform.client.ui.UiFactory.mutedText;
import static com.tfg.rentalplatform.client.ui.UiFactory.profileMetric;
import static com.tfg.rentalplatform.client.ui.ReviewUiFactory.updateStarRating;

public class RentalClientApp extends Application {

    private static final String VIEW_CATALOG = "catalogo";
    private static final String VIEW_MY_ITEMS = "mis_objetos";
    private static final String VIEW_RESERVATIONS = "reservas";
    private static final String VIEW_CHAT = "chat";
    private static final String VIEW_PAYMENTS = "pagos";
    private static final String VIEW_NOTIFICATIONS = "notificaciones";
    private static final String VIEW_PROFILE = "perfil";
    private static final String VIEW_ADMIN = "admin";
    private static final String VIEW_ADMIN_CHAT = "admin_chat";
    private static final String FILTER_ALL = "Todas";
    private static final String SORT_RECENT = "Mas recientes";
    private static final int ITEM_TITLE_MAX_LENGTH = 180;
    private static final int ITEM_DESCRIPTION_MAX_LENGTH = 1000;
    private static final int ITEM_IMAGE_MAX_LENGTH = 500;
    private static final List<String> ITEM_CATEGORIES = CatalogOptions.ITEM_CATEGORIES;
    private static final List<String> FALLBACK_PROVINCES = CatalogOptions.FALLBACK_PROVINCES;

    private final ApiClient api = new ApiClient("http://localhost:8081");
    private final Map<String, Image> imageCache = new HashMap<>();
    private final GeoDataService geoData = new GeoDataService(FALLBACK_PROVINCES);
    private final RealtimeChatClient realtimeChat = new RealtimeChatClient(
            api::getToken,
            this::handleRealtimeChatEvent,
            this::log
    );

    private Long currentUserId;
    private String currentUserName;
    private String currentUserEmail;
    private String currentUserRole;

    private Scene mainScene;
    private StackPane loginRoot;
    private StackPane appRoot;

    private Label loginStatusLabel;
    private Label sessionLabel;
    private Label toastLabel;
    private PauseTransition toastHideTransition;
    private Label headerTitleLabel;
    private Label headerSubtitleLabel;

    private TextField authNameField;
    private TextField authEmailField;
    private PasswordField authPasswordField;
    private Button authSubmitBtn;
    private SupportChatController supportChat;
    private ToggleButton loginModeBtn;
    private ToggleButton registerModeBtn;
    private VBox authNameRow;
    private VBox authFormBody;
    private Label authFormTitle;
    private Label authFormSubtitle;
    private Animation authModeTransition;
    private boolean registerMode;
    private boolean authModeReady;

    private final ObservableList<ItemRow> catalogItems = FXCollections.observableArrayList();
    private final ObservableList<ItemRow> catalogFilterSourceItems = FXCollections.observableArrayList();
    private final ObservableList<ItemRow> myItems = FXCollections.observableArrayList();
    private final ObservableList<String> reservations = FXCollections.observableArrayList();
    private final ObservableList<Long> conversations = FXCollections.observableArrayList();
    private final ObservableList<Map<String, Object>> paymentRows = FXCollections.observableArrayList();
    private final ObservableList<Map<String, Object>> notificationRows = FXCollections.observableArrayList();
    private final NotificationController notificationController = new NotificationController(
            api,
            notificationRows,
            hasUnread -> setNavAlertDot(VIEW_NOTIFICATIONS, hasUnread),
            this::log
    );
    private final Map<String, Button> navButtons = new HashMap<>();
    private final Map<String, Region> navAlertDots = new HashMap<>();

    private String currentView = VIEW_CATALOG;

    private StackPane contentHost;
    private StackPane modalOverlay;

    private FlowPane catalogCardsPane;
    private CatalogController catalogController;
    private DatePicker reserveStartPicker;
    private DatePicker reserveEndPicker;
    private Label selectedCatalogItemLabel;
    private ItemRow selectedCatalogItem;

    private TextField createTitleField;
    private TextArea createDescriptionArea;
    private ComboBox<String> createCategoryField;
    private TextField createPriceField;
    private ComboBox<String> createCityField;
    private ComboBox<String> createMunicipalityField;
    private TextField createImageField;

    private FlowPane myItemsCardsPane;
    private Label selectedMyItemLabel;
    private MyItemsController myItemsController;
    private TextField updateTitleField;
    private TextArea updateDescriptionArea;
    private ComboBox<String> updateCategoryField;
    private TextField updatePriceField;
    private ComboBox<String> updateCityField;
    private ComboBox<String> updateMunicipalityField;
    private TextField updateImageField;
    private ToggleButton updateActiveToggle;

    private ListView<String> reservationListView;
    private Label selectedReservationLabel;
    private VBox reservationDetailBox;
    private ReservationController reservationController;
    private ReviewController reviewController;

    private ChatController chatController;

    private PaymentController paymentController;

    private ListView<Map<String, Object>> notificationListView;
    private Label selectedNotificationLabel;
    private Long selectedNotificationId;

    private TextField profileNameField;
    private Label profileEmailLabel;
    private TextField profilePhoneField;
    private TextArea profileBioArea;
    private PasswordField profileCurrentPasswordField;
    private PasswordField profileNewPasswordField;
    private PasswordField profileConfirmPasswordField;
    private Label profileReputationAvatar;
    private Label profileReputationName;
    private HBox profileReputationRating;
    private HBox profileReputationStatsBox;
    private VBox profileReviewsBox;

    private TextArea adminOverviewArea;
    private TextField adminUserIdField;
    private ComboBox<String> adminRoleCombo;
    private TextField adminItemIdField;
    private CheckBox adminItemActiveCheck;
    private AdminOverviewController adminOverview;
    private ReportController reportController;

    @Override
    public void start(Stage stage) {
        BackendLauncher.ensureAvailable();
        loadGeoData();
        loginRoot = buildLoginRoot();
        appRoot = buildAppRoot();
        mainScene = new Scene(loginRoot, 1360, 900);
        mainScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        stage.setTitle("AlquilaFacil - Cliente Desktop");
        stage.setScene(mainScene);
        stage.show();
        Platform.runLater(() -> {
            Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
            stage.setX(visualBounds.getMinX());
            stage.setY(visualBounds.getMinY());
            stage.setWidth(visualBounds.getWidth());
            stage.setHeight(visualBounds.getHeight());
            stage.setMaximized(true);
        });
    }

    private StackPane buildLoginRoot() {
        VBox authPane = buildAuthPane();
        authPane.getStyleClass().add("auth-pane");

        Label welcomeTitle = new Label("AlquilaFacil");
        welcomeTitle.getStyleClass().add("welcome-title");
        Label welcomeSubtitle = new Label("Alquiler de objetos entre particulares de forma sencilla.");
        welcomeSubtitle.getStyleClass().add("welcome-subtitle");

        VBox loginBrand = new VBox(8, welcomeTitle, welcomeSubtitle);
        loginBrand.getStyleClass().add("login-brand");
        loginBrand.setAlignment(Pos.CENTER);

        VBox content = new VBox(20, loginBrand, authPane);
        content.getStyleClass().add("login-layout");
        content.setAlignment(Pos.CENTER);
        VBox.setVgrow(content, Priority.ALWAYS);

        VBox page = new VBox(content);
        page.setAlignment(Pos.CENTER);
        page.setPadding(new Insets(36));

        Button supportButton = new Button("\u260E");
        supportButton.getStyleClass().add("support-fab");
        supportButton.setOnAction(e -> supportChat.toggle());
        StackPane.setAlignment(supportButton, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(supportButton, new Insets(0, 34, 34, 0));

        supportChat = new SupportChatController(
                () -> authEmailField == null ? "" : authEmailField.getText(),
                this::log
        );
        VBox supportChatPanel = supportChat.root();
        supportChatPanel.setVisible(false);
        supportChatPanel.setManaged(false);
        StackPane.setAlignment(supportChatPanel, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(supportChatPanel, new Insets(0, 34, 104, 0));

        StackPane root = new StackPane(page, supportChatPanel, supportButton);
        root.getStyleClass().add("app-root");
        return root;
    }

    private VBox buildAuthPane() {
        AuthViewFactory.AuthViewParts view = AuthViewFactory.create(
                this::submitAuth,
                () -> setAuthMode(false),
                () -> setAuthMode(true)
        );
        authFormTitle = view.title();
        authFormSubtitle = view.subtitle();
        loginModeBtn = view.loginModeButton();
        registerModeBtn = view.registerModeButton();
        authNameField = view.nameField();
        authEmailField = view.emailField();
        authPasswordField = view.passwordField();
        authNameRow = view.nameRow();
        authSubmitBtn = view.submitButton();
        loginStatusLabel = view.statusLabel();
        authFormBody = view.body();
        setAuthMode(false);
        return view.root();
    }

    private StackPane buildAppRoot() {
        sessionLabel = new Label("Sesion: sin autenticar");
        sessionLabel.getStyleClass().add("session-label");

        headerTitleLabel = new Label("Catalogo");
        headerTitleLabel.getStyleClass().add("page-title");
        headerSubtitleLabel = new Label("Explora los objetos disponibles y crea reservas.");
        headerSubtitleLabel.getStyleClass().add("page-subtitle");

        Button logoutBtn = new Button("Cerrar sesion");
        logoutBtn.getStyleClass().add("ghost-btn");
        logoutBtn.setOnAction(e -> logout());

        VBox titleBox = new VBox(4, headerTitleLabel, headerSubtitleLabel);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox topBar = new HBox(14, titleBox, spacer, sessionLabel, logoutBtn);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.getStyleClass().add("topbar");

        VBox sidebar = buildSidebar();

        contentHost = new StackPane();
        contentHost.getStyleClass().add("content-host");

        VBox centerPane = new VBox(10, topBar, contentHost);
        centerPane.setPadding(new Insets(14, 18, 18, 18));
        VBox.setVgrow(contentHost, Priority.ALWAYS);

        BorderPane shell = new BorderPane();
        shell.setLeft(sidebar);
        shell.setCenter(centerPane);
        shell.getStyleClass().add("app-shell");

        modalOverlay = new StackPane();
        modalOverlay.getStyleClass().add("modal-overlay");
        modalOverlay.setVisible(false);
        modalOverlay.setManaged(false);
        modalOverlay.setOnMouseClicked(e -> hideModal());

        toastLabel = new Label();
        toastLabel.getStyleClass().add("toast-label");
        toastLabel.setVisible(false);
        toastLabel.setManaged(false);
        StackPane.setAlignment(toastLabel, Pos.BOTTOM_CENTER);
        StackPane.setMargin(toastLabel, new Insets(0, 0, 26, 0));

        StackPane root = new StackPane(shell, modalOverlay, toastLabel);
        root.getStyleClass().add("app-root");

        showView(isAdminRole() ? VIEW_ADMIN : VIEW_CATALOG);
        return root;
    }

    private VBox buildSidebar() {
        navButtons.clear();
        navAlertDots.clear();

        Label brand = new Label("AlquilaFacil");
        brand.getStyleClass().add("brand-title");
        Label brandSubtitle = new Label("Panel de gestion");
        brandSubtitle.getStyleClass().add("brand-subtitle");

        boolean adminMode = isAdminRole();
        VBox navBox = adminMode
                ? new VBox(10,
                        createNavButton(VIEW_ADMIN, "Administracion"),
                        createNavButton(VIEW_ADMIN_CHAT, "Chat"))
                : new VBox(10,
                createNavButton(VIEW_CATALOG, "Catalogo"),
                createNavButton(VIEW_MY_ITEMS, "Mis objetos"),
                createNavButton(VIEW_RESERVATIONS, "Reservas"),
                createNavButton(VIEW_CHAT, "Chat"),
                createNavButton(VIEW_PAYMENTS, "Pagos"),
                createNavButton(VIEW_NOTIFICATIONS, "Notificaciones"),
                createNavButton(VIEW_PROFILE, "Perfil")
        );
        navBox.getStyleClass().add("sidebar-nav");
        Region sidebarSpacer = new Region();
        VBox.setVgrow(sidebarSpacer, Priority.ALWAYS);

        Button supportBtn = new Button("Contactar soporte");
        supportBtn.getStyleClass().add("sidebar-support-btn");
        supportBtn.setMaxWidth(Double.MAX_VALUE);
        supportBtn.setOnAction(e -> openAdminChatForUser(null, "Administracion"));
        supportBtn.setVisible(!adminMode);
        supportBtn.setManaged(!adminMode);

        VBox sidebar = new VBox(24, brand, brandSubtitle, new Separator(), navBox, sidebarSpacer, supportBtn);
        sidebar.setPadding(new Insets(24));
        sidebar.getStyleClass().add("sidebar");
        return sidebar;
    }

    private Button createNavButton(String key, String label) {
        Label text = new Label(label);
        text.getStyleClass().add("nav-btn-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Region dot = new Region();
        dot.getStyleClass().addAll("nav-alert-dot", "nav-alert-dot-hidden");
        navAlertDots.put(key, dot);

        HBox content = new HBox(10, text, spacer, dot);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setMaxWidth(Double.MAX_VALUE);

        Button button = new Button();
        button.getStyleClass().add("nav-btn");
        button.setGraphic(content);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(e -> showView(key));
        navButtons.put(key, button);
        return button;
    }

    private void setNavAlertDot(String key, boolean visible) {
        Region dot = navAlertDots.get(key);
        if (dot == null) {
            return;
        }
        dot.getStyleClass().remove("nav-alert-dot-hidden");
        if (!visible) {
            dot.getStyleClass().add("nav-alert-dot-hidden");
        }
    }

    private String chatViewKey() {
        return isAdminRole() ? VIEW_ADMIN_CHAT : VIEW_CHAT;
    }

    private void showView(String viewKey) {
        currentView = viewKey;
        updateNavigationState();
        updateHeaderForView(viewKey);
        hideModal();
        contentHost.getChildren().setAll(buildView(viewKey));
    }

    private Node buildView(String viewKey) {
        return switch (viewKey) {
            case VIEW_CATALOG -> buildCatalogView();
            case VIEW_MY_ITEMS -> buildMyItemsView();
            case VIEW_RESERVATIONS -> buildReservationsView();
            case VIEW_CHAT -> buildChatView();
            case VIEW_PAYMENTS -> buildPaymentsView();
            case VIEW_NOTIFICATIONS -> buildNotificationsView();
            case VIEW_PROFILE -> buildProfileView();
            case VIEW_ADMIN -> buildAdminView();
            case VIEW_ADMIN_CHAT -> buildAdminChatView();
            default -> buildCatalogView();
        };
    }

    private void updateNavigationState() {
        navButtons.forEach((key, button) -> {
            button.getStyleClass().remove("nav-btn-active");
            if (key.equals(currentView)) {
                button.getStyleClass().add("nav-btn-active");
            }
        });
        boolean admin = "ADMIN".equalsIgnoreCase(currentUserRole);
        Button adminButton = navButtons.get(VIEW_ADMIN);
        if (adminButton != null) {
            adminButton.setDisable(!admin);
        }
    }

    private void updateHeaderForView(String viewKey) {
        switch (viewKey) {
            case VIEW_CATALOG -> {
                headerTitleLabel.setText("Catalogo");
                headerSubtitleLabel.setText("Explora los objetos disponibles y crea reservas.");
            }
            case VIEW_MY_ITEMS -> {
                headerTitleLabel.setText("Mis objetos");
                headerSubtitleLabel.setText("Publica, edita y controla tu inventario.");
            }
            case VIEW_RESERVATIONS -> {
                headerTitleLabel.setText("Reservas");
                headerSubtitleLabel.setText("Gestiona solicitudes como inquilino o propietario.");
            }
            case VIEW_CHAT -> {
                headerTitleLabel.setText("Chat");
                headerSubtitleLabel.setText("Habla con inquilinos y propietarios sobre tus reservas.");
            }
            case VIEW_PAYMENTS -> {
                headerTitleLabel.setText("Gastos e ingresos");
                headerSubtitleLabel.setText("Consulta lo que has pagado como inquilino y lo que has cobrado como propietario.");
            }
            case VIEW_NOTIFICATIONS -> {
                headerTitleLabel.setText("Notificaciones");
                headerSubtitleLabel.setText("Haz seguimiento de toda la actividad relevante.");
            }
            case VIEW_PROFILE -> {
                headerTitleLabel.setText("Perfil");
                headerSubtitleLabel.setText("Gestiona tus datos, reputacion y actividad publica.");
            }
            case VIEW_ADMIN -> {
                headerTitleLabel.setText("Administracion");
                headerSubtitleLabel.setText("Supervisa usuarios, catalogo y estado general.");
            }
            case VIEW_ADMIN_CHAT -> {
                headerTitleLabel.setText("Chat");
                headerSubtitleLabel.setText("Contacta con usuarios desde administracion.");
            }
            default -> {
                headerTitleLabel.setText("Plataforma");
                headerSubtitleLabel.setText("Gestion central del sistema.");
            }
        }
    }

    private Node buildCatalogView() {
        CatalogViewFactory.CatalogViewParts view = CatalogViewFactory.create(
                FILTER_ALL,
                SORT_RECENT,
                this::showCatalogSuggestions,
                this::refreshCatalogFromControls,
                this::clearCatalogSearchText,
                this::clearCatalogFilters,
                this::onCatalogCategoryFilterChanged,
                this::onCatalogCityFilterChanged,
                this::onCatalogMunicipalityFilterChanged,
                this::onCatalogSortChanged,
                width -> syncCardGridWidth(catalogCardsPane, width)
        );
        catalogCardsPane = view.cardsPane();
        catalogController = new CatalogController(
                api,
                catalogItems,
                catalogFilterSourceItems,
                ITEM_CATEGORIES,
                FILTER_ALL,
                SORT_RECENT,
                this::provinceOptions,
                geoData.municipalitiesByProvince(),
                this::isLogged,
                this::renderCatalogCards,
                this::log
        );
        catalogController.bind(view);
        renderCatalogCards();
        loadCatalogFilterSource();
        return view.root();
    }

    private void onCatalogCategoryFilterChanged() {
        if (catalogController != null) {
            catalogController.onCategoryChanged();
        }
    }

    private void onCatalogCityFilterChanged() {
        if (catalogController != null) {
            catalogController.onCityChanged();
        }
    }

    private void onCatalogMunicipalityFilterChanged() {
        if (catalogController != null) {
            catalogController.onMunicipalityChanged();
        }
    }

    private void onCatalogSortChanged() {
        if (catalogController != null) {
            catalogController.onSortChanged();
        }
    }

    private Node buildMyItemsView() {
        MyItemsViewFactory.MyItemsViewParts view = MyItemsViewFactory.create(
                this::openCreateItemModal,
                width -> syncCardGridWidth(myItemsCardsPane, width)
        );
        myItemsCardsPane = view.cardsPane();
        renderMyItemsCards();
        return view.root();
    }

    private MyItemsController myItemsController() {
        if (myItemsController == null) {
            myItemsController = new MyItemsController(
                    api,
                    myItems,
                    this::isLogged,
                    () -> VIEW_MY_ITEMS.equals(currentView),
                    this::renderMyItemsCards,
                    this::refreshCatalog,
                    this::hideModal,
                    () -> showView(VIEW_MY_ITEMS),
                    this::log
            );
        }
        return myItemsController;
    }

    private ItemRow selectedMyItem() {
        return myItemsController == null ? null : myItemsController.selectedItem();
    }

    private Node buildReservationsView() {
        ReservationsViewFactory.ReservationsViewParts view = ReservationsViewFactory.create(
                reservations,
                reservationController().currentType(),
                this::reservationListGraphic,
                this::selectReservation,
                this::loadMyReservations,
                this::loadOwnerReservations
        );
        reservationListView = view.listView();
        selectedReservationLabel = view.selectedLabel();
        reservationDetailBox = view.detailBox();
        renderReservationDetail();
        return view.root();
    }

    private ReservationController reservationController() {
        if (reservationController == null) {
            reservationController = new ReservationController(
                    api,
                    reservations,
                    this::isLogged,
                    this::reservationContactText,
                    this::renderReservationDetail,
                    this::loadPayerPayments,
                    this::loadNotifications,
                    this::log
            );
        }
        return reservationController;
    }

    private Node reservationListGraphic(String item) {
        Long reservationId = extractLeadingId(item);
        Map<String, Object> reservation = reservationController().rowById(reservationId);
        return reservation == null ? new Label(item) : ActivityRowFactory.reservationRow(reservation, reservationContactText(reservation));
    }

    private void selectReservation(String value) {
        reservationController().select(extractLeadingId(value));
        Long selectedReservationId = reservationController().selectedId();
        if (selectedReservationLabel != null) {
            selectedReservationLabel.setText(selectedReservationId == null ? "Sin reserva seleccionada" : "Reserva " + selectedReservationId);
        }
        renderReservationDetail();
    }

    private Node buildChatView() {
        return chatController().createView();
    }

    private ChatController chatController() {
        if (chatController == null) {
            chatController = new ChatController(
                    api,
                    conversations,
                    this::isLogged,
                    this::isAdminRole,
                    hasUnread -> setNavAlertDot(chatViewKey(), hasUnread),
                    () -> showView(chatViewKey()),
                    this::openItemDetailById,
                    this::openPublicProfileModal,
                    this::hideModal,
                    this::log
            );
        }
        return chatController;
    }

    private void selectConversation(Long conversationId) {
        if (chatController != null) {
            chatController.selectConversation(conversationId);
        }
    }

    private Node buildPaymentsView() {
        paymentController = new PaymentController(
                api,
                paymentRows,
                this::isLogged,
                this::log
        );
        return paymentController.createView();
    }

    private Node buildNotificationsView() {
        NotificationsViewFactory.NotificationsViewParts view = NotificationsViewFactory.create(
                notificationRows,
                this::markNotificationRead,
                this::markAllNotificationsRead
        );
        notificationListView = view.listView();
        loadNotifications();
        return view.root();
    }

    private Node buildProfileView() {
        ProfileViewFactory.ProfileViewParts view = ProfileViewFactory.create(
                currentUserName,
                currentUserEmail,
                profileInitial(currentUserName),
                this::buildFieldGroup,
                this::updateProfile,
                this::changePassword
        );
        profileNameField = view.nameField();
        profileEmailLabel = view.emailLabel();
        profilePhoneField = view.phoneField();
        profileBioArea = view.bioArea();
        profileCurrentPasswordField = view.currentPasswordField();
        profileNewPasswordField = view.newPasswordField();
        profileConfirmPasswordField = view.confirmPasswordField();
        profileReputationAvatar = view.reputationAvatar();
        profileReputationName = view.reputationName();
        profileReputationRating = view.reputationRating();
        profileReputationStatsBox = view.reputationStatsBox();
        profileReviewsBox = view.reviewsBox();
        loadProfile();
        return view.root();
    }

    private Node buildAdminView() {
        adminOverview = new AdminOverviewController(
                api,
                this::isAdmin,
                this::log,
                this::openItemDetailById,
                this::openPublicProfileModal,
                this::openAdminChatForUser
        );
        Node root = adminOverview.createView();
        loadAdminOverview();
        return root;
    }

    private Node buildAdminChatView() {
        return buildChatView();
    }

    private VBox buildFieldGroup(String labelText, Node control) {
        return UiFactory.fieldGroup(labelText, control, FormControlHelper::attachFeedbackClearListener);
    }

    private VBox buildFieldGroup(String labelText, Node control, Node feedbackTarget) {
        return UiFactory.fieldGroup(labelText, control, feedbackTarget, FormControlHelper::attachFeedbackClearListener);
    }

    private void showModal(String title, String subtitle, Node content) {
        showModal(title, subtitle, content, "modal-card", 620, 520);
    }

    private void showModal(String title, String subtitle, Node content, String cardStyle, double width, double maxContentHeight) {
        showModal(title, subtitle, content, null, cardStyle, width, maxContentHeight);
    }

    private void showModal(String title, String subtitle, Node content, Node footer, String cardStyle, double width, double maxContentHeight) {
        VBox card = ModalFactory.modalCard(title, subtitle, content, footer, cardStyle, width, maxContentHeight, this::hideModal);
        modalOverlay.getChildren().setAll(card);
        modalOverlay.setVisible(true);
        modalOverlay.setManaged(true);
    }

    private void hideModal() {
        if (modalOverlay != null) {
            modalOverlay.getChildren().clear();
            modalOverlay.setVisible(false);
            modalOverlay.setManaged(false);
        }
    }

    private void openCreateItemModal() {
        createTitleField = new TextField();
        FormControlHelper.limitTextInput(createTitleField, ITEM_TITLE_MAX_LENGTH);
        createDescriptionArea = new TextArea();
        FormControlHelper.configureDescriptionArea(createDescriptionArea, ITEM_DESCRIPTION_MAX_LENGTH);
        createCategoryField = categoryCombo(null);
        createPriceField = new TextField();
        FormControlHelper.configurePriceField(createPriceField);
        createCityField = provinceCombo(null);
        createMunicipalityField = municipalityCombo(null, null);
        createCityField.setOnAction(e -> updateMunicipalityOptions(createMunicipalityField, createCityField.getValue(), null));
        createImageField = new TextField();
        FormControlHelper.limitTextInput(createImageField, ITEM_IMAGE_MAX_LENGTH);

        Button publishBtn = new Button("Publicar objeto");
        publishBtn.getStyleClass().add("primary-btn");
        publishBtn.setOnAction(e -> createItem());

        Region actionSpacer = new Region();
        HBox.setHgrow(actionSpacer, Priority.ALWAYS);
        HBox actions = new HBox(10, publishBtn, actionSpacer);
        actions.setAlignment(Pos.CENTER_LEFT);

        VBox form = new VBox(14,
                buildFieldGroup("Titulo", createTitleField),
                buildFieldGroup("Descripcion", createDescriptionArea),
                buildFieldGroup("Categoria", createCategoryField),
                buildFieldGroup("Precio por dia", createPriceField),
                buildFieldGroup("Provincia", createCityField),
                buildFieldGroup("Municipio", createMunicipalityField),
                buildFieldGroup("Imagen", imagePickerControl(createImageField), createImageField)
        );
        form.getStyleClass().add("form-stack");

        showModal("Publicar objeto", "Crea un nuevo anuncio cuando lo necesites.", form, actions, "modal-card", 620, 520);
    }

    private void openEditItemModal() {
        ItemRow selectedMyItem = selectedMyItem();
        if (selectedMyItem == null) {
            log("Selecciona primero uno de tus objetos para editarlo.");
            return;
        }

        updateTitleField = new TextField(selectedMyItem.title());
        FormControlHelper.limitTextInput(updateTitleField, ITEM_TITLE_MAX_LENGTH);
        updateDescriptionArea = new TextArea(selectedMyItem.description());
        FormControlHelper.configureDescriptionArea(updateDescriptionArea, ITEM_DESCRIPTION_MAX_LENGTH);
        updateCategoryField = categoryCombo(displayCategory(selectedMyItem.category()));
        updatePriceField = new TextField(selectedMyItem.pricePerDay());
        FormControlHelper.configurePriceField(updatePriceField);
        updateCityField = provinceCombo(selectedMyItem.city());
        updateMunicipalityField = municipalityCombo(selectedMyItem.city(), selectedMyItem.municipality());
        updateCityField.setOnAction(e -> updateMunicipalityOptions(updateMunicipalityField, updateCityField.getValue(), null));
        updateImageField = new TextField(selectedMyItem.imageUrl());
        FormControlHelper.limitTextInput(updateImageField, ITEM_IMAGE_MAX_LENGTH);
        updateActiveToggle = new ToggleButton();
        updateActiveToggle.getStyleClass().add("status-toggle");
        updateActiveToggle.setSelected(selectedMyItem.active());
        updateActiveToggle.setMaxWidth(Double.MAX_VALUE);
        updateActiveToggle.setDisable(selectedMyItem.moderationBlocked());
        updateActiveToggle.selectedProperty().addListener((obs, oldValue, selected) -> updateActiveToggleText());
        updateActiveToggleText();

        Button updateBtn = new Button("Guardar cambios");
        updateBtn.getStyleClass().add("primary-btn");
        updateBtn.setOnAction(e -> updateSelectedItem());

        Button deleteBtn = new Button("Eliminar objeto");
        deleteBtn.getStyleClass().add("danger-btn");
        deleteBtn.setOnAction(e -> deleteSelectedItem());

        Region actionSpacer = new Region();
        HBox.setHgrow(actionSpacer, Priority.ALWAYS);
        HBox actions = new HBox(10, updateBtn, actionSpacer, deleteBtn);
        actions.setAlignment(Pos.CENTER_LEFT);

        VBox form = new VBox(14,
                buildFieldGroup("Titulo", updateTitleField),
                buildFieldGroup("Descripcion", updateDescriptionArea),
                buildFieldGroup("Categoria", updateCategoryField),
                buildFieldGroup("Precio por dia", updatePriceField),
                buildFieldGroup("Provincia", updateCityField),
                buildFieldGroup("Municipio", updateMunicipalityField),
                buildFieldGroup("Imagen", imagePickerControl(updateImageField), updateImageField),
                buildFieldGroup("Estado del anuncio", updateActiveToggle),
                selectedMyItem.moderationBlocked()
                        ? mutedText("Este objeto ha sido retirado por administracion y no puede volver al catalogo.")
                        : new Region()
        );
        form.getStyleClass().add("form-stack");

        showModal("Editar objeto", "Actualiza la informacion del anuncio seleccionado.", form, actions, "modal-card", 720, 520);
    }

    private HBox imagePickerControl(TextField imageField) {
        imageField.setMaxWidth(Double.MAX_VALUE);
        Button chooseBtn = new Button("Seleccionar");
        chooseBtn.setOnAction(e -> chooseLocalImage(imageField));
        HBox row = new HBox(10, imageField, chooseBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(imageField, Priority.ALWAYS);
        return row;
    }

    private void chooseLocalImage(TextField imageField) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Seleccionar imagen");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "Imagenes", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp", "*.bmp"
        ));
        File file = chooser.showOpenDialog(mainScene == null ? null : mainScene.getWindow());
        if (file != null) {
            imageField.setText(file.toURI().toString());
        }
    }

    private void loadGeoData() {
        geoData.load(this);
    }

    private void loadFallbackGeoData() {
        geoData.loadFallback();
    }

    private List<String> provinceOptions() {
        return geoData.provinceOptions();
    }

    private ComboBox<String> categoryCombo(String selectedCategory) {
        return GeoComboFactory.categoryCombo(ITEM_CATEGORIES, selectedCategory);
    }

    private ComboBox<String> provinceCombo(String selectedProvince) {
        return geoData.provinceCombo(selectedProvince);
    }

    private ComboBox<String> municipalityCombo(String province, String selectedMunicipality) {
        return geoData.municipalityCombo(province, selectedMunicipality);
    }

    private void updateMunicipalityOptions(ComboBox<String> combo, String province, String selectedMunicipality) {
        geoData.updateMunicipalityOptions(combo, province, selectedMunicipality);
    }

    private String resolveProvinceName(String province) {
        return geoData.resolveProvinceName(province);
    }

    private void updateActiveToggleText() {
        if (updateActiveToggle == null) {
            return;
        }
        ItemRow selectedMyItem = selectedMyItem();
        if (selectedMyItem != null && selectedMyItem.moderationBlocked()) {
            updateActiveToggle.setText("Retirado por administracion");
            return;
        }
        updateActiveToggle.setText(updateActiveToggle.isSelected()
                ? "Publicado en catalogo"
                : "Pausado, no visible en catalogo");
    }

    private void updateBookingSummary(ItemRow item, LocalDate start, LocalDate end, Label totalAmount, Label totalBreakdown, Label totalNote) {
        BookingSummary.update(item, start, end, totalAmount, totalBreakdown, totalNote);
    }

    private void openCreateReviewModalForSelectedReservation() {
        Long selectedReservationId = reservationController().selectedId();
        Map<String, Object> selectedReservationData = reservationController().selectedData();
        if (selectedReservationData == null || selectedReservationId == null) {
            log("Selecciona una reserva completada para valorar.");
            return;
        }
        String type = String.valueOf(selectedReservationData.get("_type"));
        String status = String.valueOf(selectedReservationData.get("status"));
        if (!"Inquilino".equals(type)) {
            log("Solo el inquilino puede valorar al propietario desde sus reservas.");
            return;
        }
        if (!"COMPLETED".equals(status)) {
            log("Solo puedes valorar cuando el alquiler este completado.");
            return;
        }

        Map<String, Object> existingReview = findMyReviewForReservation(selectedReservationId);
        Long existingReviewId = existingReview == null ? null : toLong(existingReview.get("id"));

        ComboBox<Integer> ratingCombo = new ComboBox<>(FXCollections.observableArrayList(0, 1, 2, 3, 4, 5));
        ratingCombo.setValue(existingReview == null ? 5 : toInt(existingReview.get("rating")));
        ratingCombo.setMaxWidth(Double.MAX_VALUE);
        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Cuenta como ha sido la experiencia con el propietario.");
        if (existingReview != null) {
            commentArea.setText(nullableString(existingReview.get("comment")));
        }
        commentArea.setPrefRowCount(4);

        String reviewItemTitle = nullableString(selectedReservationData.get("itemTitle"));
        String reviewDates = formatReservationDates(selectedReservationData);
        Label reservationInfo = new Label(reviewItemTitle.isBlank()
                ? "Alquiler completado"
                : reviewDates.isBlank() ? reviewItemTitle : reviewItemTitle + " - " + reviewDates);
        reservationInfo.getStyleClass().add("selection-info");

        Button createBtn = new Button(existingReviewId == null ? "Publicar resena" : "Guardar cambios");
        createBtn.getStyleClass().add("primary-btn");
        createBtn.setOnAction(e -> saveReview(selectedReservationId, existingReviewId, ratingCombo.getValue(), commentArea.getText()));

        VBox form = new VBox(14,
                buildFieldGroup("Reserva completada", reservationInfo),
                buildFieldGroup("Puntuacion", ratingCombo),
                buildFieldGroup("Comentario", commentArea),
                createBtn
        );
        form.getStyleClass().add("form-stack");

        showModal(existingReviewId == null ? "Valorar propietario" : "Editar valoracion",
                existingReviewId == null
                        ? "La resena se publicara en el perfil del propietario."
                        : "Actualiza la valoracion que ya publicaste para este alquiler.",
                form);
    }

    private void openCatalogDetailModal(ItemRow item) {
        selectedCatalogItem = item;
        boolean adminMode = isAdminRole();
        CatalogDetailModalFactory.CatalogDetailParts view = CatalogDetailModalFactory.create(
                item,
                adminMode,
                !adminMode && (item.ownerId() == null || !item.ownerId().equals(currentUserId)),
                imageCache,
                this::buildFieldGroup,
                (detailItem, start, end, totalAmount, totalBreakdown, totalNote) ->
                        updateBookingSummary(detailItem, start, end, totalAmount, totalBreakdown, totalNote),
                () -> openReportModal("ITEM", item.id(), null, item.title()),
                () -> openPublicProfileModal(item.ownerId()),
                this::openChatForCatalogItem,
                this::createReservation
        );
        reserveStartPicker = view.startPicker();
        reserveEndPicker = view.endPicker();

        showModal(adminMode ? "Detalle del anuncio" : "Detalle y reserva",
                adminMode ? "Revisa la informacion del objeto reportado." : "Consulta el objeto, elige fechas y confirma la solicitud.",
                view.content(),
                "modal-card modal-card-detail",
                1080,
                560);
    }

    private Button reportUserButton(Long userId, String userName) {
        boolean ownProfile = userId != null && userId.equals(currentUserId);
        boolean adminMode = isAdminRole();
        Button reportBtn = new Button(ownProfile ? "Tu perfil" : adminMode ? "Contactar" : "Reportar");
        reportBtn.getStyleClass().add("report-secondary-btn");
        reportBtn.setMinWidth(92);
        reportBtn.setPrefWidth(92);
        reportBtn.setDisable(ownProfile);
        reportBtn.setVisible(userId != null);
        reportBtn.setManaged(reportBtn.isVisible());
        reportBtn.setOnAction(e -> {
            if (adminMode) {
                openAdminChatForUser(userId, userName);
            } else {
                openReportModal("USER", null, userId, userName);
            }
        });
        return reportBtn;
    }

    private void openReportModal(String type, Long itemId, Long userId, String targetName) {
        if (!isLogged()) {
            return;
        }
        ReportModalFactory.ReportModalParts reportModal = ReportModalFactory.create(
                targetName,
                this::buildFieldGroup,
                (reason, details) -> submitReport(type, itemId, userId, reason, details)
        );

        showModal("Enviar reporte",
                "El equipo de administracion revisara la informacion enviada.",
                reportModal.content(),
                "modal-card",
                560,
                460);
    }

    private void setAuthMode(boolean registerMode) {
        if (authModeReady && this.registerMode == registerMode) {
            restoreAuthAnimationState();
            loginModeBtn.setSelected(!registerMode);
            registerModeBtn.setSelected(registerMode);
            return;
        }
        if (!authModeReady || authFormBody == null) {
            applyAuthMode(registerMode);
            authModeReady = true;
            return;
        }

        restoreAuthAnimationState();
        double direction = registerMode ? 1 : -1;
        FadeTransition fadeOut = new FadeTransition(Duration.millis(110), authFormBody);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(110), authFormBody);
        slideOut.setFromY(0);
        slideOut.setToY(-8 * direction);

        ParallelTransition out = new ParallelTransition(fadeOut, slideOut);
        out.setOnFinished(e -> {
            applyAuthMode(registerMode);
            authFormBody.setTranslateY(8 * direction);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(180), authFormBody);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            TranslateTransition slideIn = new TranslateTransition(Duration.millis(180), authFormBody);
            slideIn.setFromY(8 * direction);
            slideIn.setToY(0);
            authModeTransition = new ParallelTransition(fadeIn, slideIn);
            authModeTransition.setOnFinished(done -> restoreAuthAnimationState());
            authModeTransition.play();
        });
        authModeTransition = out;
        out.play();
    }

    private void restoreAuthAnimationState() {
        if (authModeTransition != null) {
            authModeTransition.stop();
            authModeTransition = null;
        }
        if (authFormBody != null) {
            authFormBody.setOpacity(1);
            authFormBody.setTranslateY(0);
        }
    }

    private void applyAuthMode(boolean registerMode) {
        this.registerMode = registerMode;
        authNameRow.setVisible(registerMode);
        if (registerMode) {
            loginModeBtn.setSelected(false);
            registerModeBtn.setSelected(true);
            authFormTitle.setText("Crear cuenta");
            authFormSubtitle.setText("Crea tu perfil para empezar a publicar y reservar.");
            authSubmitBtn.setText("Crear cuenta");
        } else {
            loginModeBtn.setSelected(true);
            registerModeBtn.setSelected(false);
            authFormTitle.setText("Iniciar sesion");
            authFormSubtitle.setText("Entra a tu espacio de trabajo.");
            authSubmitBtn.setText("Entrar");
        }
        showLoginStatus("");
    }

    private void submitAuth() {
        if (registerMode) {
            register(authNameField.getText(), authEmailField.getText(), authPasswordField.getText());
        } else {
            login(authEmailField.getText(), authPasswordField.getText());
        }
    }

    private void login(String email, String password) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            showLoginStatus("Completa email y contrasena.");
            return;
        }
        authSubmitBtn.setDisable(true);
        authSubmitBtn.setText("Entrando...");
        try {
            Map<String, Object> response = api.post("/api/auth/login", Map.of(
                    "email", email,
                    "password", password
            ));
            onAuthSuccess(response);
        } catch (Exception ex) {
            showLoginStatus(loginMessage(ex.getMessage()));
            log("Error en login: " + ex.getMessage());
        } finally {
            authSubmitBtn.setDisable(false);
            authSubmitBtn.setText("Entrar");
        }
    }

    private void register(String name, String email, String password) {
        if (name == null || name.isBlank() || email == null || email.isBlank() || password == null || password.isBlank()) {
            showLoginStatus("Rellena nombre, email y contrasena.");
            return;
        }
        authSubmitBtn.setDisable(true);
        authSubmitBtn.setText("Creando cuenta...");
        try {
            Map<String, Object> response = api.post("/api/auth/register", Map.of(
                    "name", name,
                    "email", email,
                    "password", password
            ));
            onAuthSuccess(response);
            showLoginStatus("Usuario registrado correctamente");
            log("Usuario registrado");
        } catch (Exception ex) {
            showLoginStatus(registerMessage(ex.getMessage()));
            log("Error en registro: " + ex.getMessage());
        } finally {
            authSubmitBtn.setDisable(false);
            authSubmitBtn.setText("Crear cuenta");
        }
    }

    @SuppressWarnings("unchecked")
    private void onAuthSuccess(Map<String, Object> response) {
        String token = String.valueOf(response.get("token"));
        Map<String, Object> user = (Map<String, Object>) response.get("user");
        api.setToken(token);
        currentUserId = toLong(user.get("id"));
        currentUserName = String.valueOf(user.get("name"));
        currentUserEmail = String.valueOf(user.get("email"));
        currentUserRole = String.valueOf(user.get("role"));

        sessionLabel.setText("Sesion: " + currentUserName);
        showLoginStatus("");
        appRoot = buildAppRoot();

        sessionLabel.setText("Sesion: " + currentUserName);

        if (isAdminRole()) {
            Platform.runLater(this::loadAdminOverview);
            connectRealtimeChat();
        } else {
            refreshCatalog();
            loadMyItems();
            loadMyReservations();
            loadNotifications();
            loadPayerPayments();
            loadConversations();
            connectRealtimeChat();
        }

        mainScene.setRoot(appRoot);
        log("Autenticacion correcta para " + user.get("email"));
    }

    private void logout() {
        currentUserId = null;
        currentUserName = null;
        currentUserEmail = null;
        currentUserRole = null;
        disconnectRealtimeChat();
        api.setToken(null);

        catalogItems.clear();
        myItems.clear();
        reservations.clear();
        clearChatState();
        if (paymentController != null) {
            paymentController.clear();
        } else {
            paymentRows.clear();
        }
        notificationRows.clear();
        setNavAlertDot(VIEW_CHAT, false);
        setNavAlertDot(VIEW_NOTIFICATIONS, false);
        if (reservationController != null) {
            reservationController.clear();
        } else {
            reservations.clear();
        }
        selectedCatalogItem = null;
        if (myItemsController != null) {
            myItemsController.clearSelection();
        }
        selectedNotificationId = null;

        showLoginStatus("");
        hideModal();
        mainScene.setRoot(loginRoot);
        log("Sesion cerrada");
    }

    private void showLoginStatus(String message) {
        if (loginStatusLabel != null) {
            loginStatusLabel.setText(message == null ? "" : message);
        }
    }

    private void refreshCatalog() {
        if (catalogController != null) {
            catalogController.refresh();
        }
    }

    private void refreshCatalog(String query, String category, String city, String municipality) {
        if (catalogController != null) {
            catalogController.refresh(query, category, city, municipality);
        }
    }

    private void refreshCatalogFromControls() {
        if (catalogController != null) {
            catalogController.refreshFromControls();
        }
    }

    private void clearCatalogFilters() {
        if (catalogController != null) {
            catalogController.clearFilters();
        }
    }

    private void clearCatalogSearchText() {
        if (catalogController != null) {
            catalogController.clearSearchText();
        }
    }

    private void loadCatalogFilterSource() {
        if (catalogController != null) {
            catalogController.loadFilterSource();
        }
    }

    private void showCatalogSuggestions(String text) {
        if (catalogController != null) {
            catalogController.showSuggestions(text);
        }
    }

    private void createItem() {
        ItemFormHelper.ItemFormFields fields = createItemFormFields();
        if (!validateItemForm(fields)) {
            return;
        }
        myItemsController().create(fields);
    }

    private boolean validateItemForm(ItemFormHelper.ItemFormFields fields) {
        return ItemFormHelper.validate(
                fields,
                ITEM_IMAGE_MAX_LENGTH,
                FormControlHelper::clearFieldFeedback,
                FormControlHelper::setFieldFeedback,
                () -> log("Revisa los campos marcados antes de guardar.")
        );
    }

    private ItemFormHelper.ItemFormFields createItemFormFields() {
        return new ItemFormHelper.ItemFormFields(
                createTitleField,
                createDescriptionArea,
                createCategoryField,
                createPriceField,
                createCityField,
                createMunicipalityField,
                createImageField
        );
    }

    private ItemFormHelper.ItemFormFields updateItemFormFields() {
        return new ItemFormHelper.ItemFormFields(
                updateTitleField,
                updateDescriptionArea,
                updateCategoryField,
                updatePriceField,
                updateCityField,
                updateMunicipalityField,
                updateImageField
        );
    }

    private void loadMyItems() {
        myItemsController().load();
    }

    private void fillEditForm(ItemRow item) {
        if (item == null) {
            return;
        }
        myItemsController().selectItem(item);
        if (selectedMyItemLabel != null) {
            selectedMyItemLabel.setText("#" + item.id() + " | " + item.title());
        }
        if (updateTitleField != null) {
            updateTitleField.setText(item.title());
        }
        if (updateDescriptionArea != null) {
            updateDescriptionArea.setText(item.description());
        }
        if (updateCategoryField != null) {
            updateCategoryField.setValue(displayCategory(item.category()));
        }
        if (updatePriceField != null) {
            updatePriceField.setText(item.pricePerDay());
        }
        if (updateCityField != null) {
            if (!updateCityField.getItems().contains(item.city())) {
                updateCityField.getItems().add(item.city());
            }
            updateCityField.setValue(item.city());
        }
        if (updateMunicipalityField != null) {
            updateMunicipalityOptions(updateMunicipalityField, item.city(), item.municipality());
        }
        if (updateImageField != null) {
            updateImageField.setText(item.imageUrl());
        }
        if (updateActiveToggle != null) {
            updateActiveToggle.setSelected(item.active());
            updateActiveToggle.setDisable(item.moderationBlocked());
            updateActiveToggleText();
        }
    }

    private void updateSelectedItem() {
        ItemFormHelper.ItemFormFields fields = updateItemFormFields();
        if (!validateItemForm(fields)) {
            return;
        }
        myItemsController().update(fields, updateActiveToggle.isSelected());
    }

    private void deleteSelectedItem() {
        myItemsController().deleteSelected();
    }

    private void createReservation() {
        if (!isLogged()) {
            return;
        }
        if (selectedCatalogItem == null) {
            log("Selecciona un objeto del catalogo para reservar.");
            return;
        }
        if (reserveStartPicker == null || reserveEndPicker == null
                || reserveStartPicker.getValue() == null || reserveEndPicker.getValue() == null) {
            log("Selecciona las fechas de inicio y fin.");
            return;
        }
        if (!reserveEndPicker.getValue().isAfter(reserveStartPicker.getValue())) {
            log("La fecha de fin debe ser posterior a la fecha de inicio.");
            return;
        }
        try {
            long itemId = selectedCatalogItem.id();
            api.post("/api/reservations", Map.of(
                    "itemId", itemId,
                    "startDate", reserveStartPicker.getValue().toString(),
                    "endDate", reserveEndPicker.getValue().toString()
            ));
            log("Reserva solicitada correctamente.");
            loadMyReservations();
            loadNotifications();
            hideModal();
        } catch (Exception ex) {
            log("Error en reserva: " + ex.getMessage());
        }
    }

    private void loadMyReservations() {
        reservationController().loadRenterReservations();
    }

    private void loadOwnerReservations() {
        reservationController().loadOwnerReservations();
    }

    private void reservationAction(String action) {
        reservationController().action(action);
    }

    private void reloadCurrentReservations() {
        reservationController().reloadCurrent();
    }

    private String reservationContactText(Map<String, Object> reservation) {
        return ReservationText.contactText(
                reservation,
                reservationController().currentType(),
                catalogItems,
                catalogFilterSourceItems,
                myItems
        );
    }

    private void hideSelectedReservationFromList() {
        reservationController().hideSelectedFromList();
    }

    private void renderReservationDetail() {
        if (reservationDetailBox == null) {
            return;
        }
        while (reservationDetailBox.getChildren().size() > 3) {
            reservationDetailBox.getChildren().remove(3);
        }
        ReservationDetailFactory.DetailContent detail = ReservationDetailFactory.create(
                reservationController().selectedData(),
                this::reservationContactText,
                new ReservationDetailFactory.ReservationActions(
                        () -> reservationAction("accept"),
                        () -> reservationAction("reject"),
                        () -> reservationAction("complete"),
                        () -> reservationAction("cancel"),
                        this::openCreateReviewModalForSelectedReservation,
                        this::openChatForSelectedReservation,
                        this::hideSelectedReservationFromList
                )
        );
        if (selectedReservationLabel != null) {
            selectedReservationLabel.setText(detail.labelText());
        }
        reservationDetailBox.getChildren().addAll(detail.nodes());
    }

    private void openChatForSelectedReservation() {
        chatController().openForReservation(reservationController().selectedId());
    }

    private void openChatForCatalogItem(ItemRow item) {
        chatController().openForCatalogItem(item);
    }

    private void loadConversations() {
        chatController().loadConversations();
    }

    private void openAdminChatForUser(Long userId, String userName) {
        chatController().openAdminChatForUser(userId, userName);
    }

    private void openItemDetailById(Long itemId) {
        if (itemId == null) {
            return;
        }
        try {
            Map<String, Object> item = api.getMap("/api/items/" + itemId);
            openCatalogDetailModal(ItemMapper.toItemRow(item));
        } catch (Exception ex) {
            log("Error al abrir objeto: " + ex.getMessage());
        }
    }

    private void connectRealtimeChat() {
        realtimeChat.connect();
    }

    @SuppressWarnings("unchecked")
    private void handleRealtimeChatEvent(Map<String, Object> event) {
        String type = String.valueOf(event.get("type"));
        if ("RESERVATION_UPDATED".equals(type)) {
            reloadCurrentReservations();
            loadNotifications();
            loadConversations();
            log("Reserva actualizada");
            return;
        }
        if ("NOTIFICATION_CREATED".equals(type)) {
            setNavAlertDot(VIEW_NOTIFICATIONS, true);
            if (isAdminRole()) {
                loadAdminOverview();
            }
            if (VIEW_NOTIFICATIONS.equals(currentView)) {
                loadNotifications();
            }
            log("Nueva notificacion recibida");
            return;
        }
        if (!"CHAT_MESSAGE".equals(type)) {
            return;
        }
        Map<String, Object> message = (Map<String, Object>) event.get("payload");
        chatController().handleRealtimeMessage(message);
        log("Nuevo mensaje recibido");
    }

    private void disconnectRealtimeChat() {
        realtimeChat.disconnect();
    }

    private void loadPayerPayments() {
        if (paymentController != null) {
            paymentController.loadPayerPayments();
        }
    }

    private void clearChatState() {
        if (chatController != null) {
            chatController.clear();
        } else {
            conversations.clear();
        }
    }

    private void loadNotifications() {
        if (!isLogged()) {
            return;
        }
        notificationController.load();
    }

    private void markNotificationRead() {
        if (!isLogged()) {
            return;
        }
        notificationController.markRead(selectedNotificationId);
    }

    private void markNotificationRead(Long notificationId) {
        if (!isLogged() || notificationId == null) {
            return;
        }
        notificationController.markReadSilently(notificationId);
    }

    private void markAllNotificationsRead() {
        if (!isLogged()) {
            return;
        }
        notificationController.markAllRead();
    }

    private Map<String, Object> findMyReviewForReservation(Long reservationId) {
        return reviewController().findForReservation(reservationId);
    }

    private void saveReview(Long reservationId, Long reviewId, Integer rating, String comment) {
        reviewController().save(reservationId, reviewId, rating, comment);
    }

    private ReviewController reviewController() {
        if (reviewController == null) {
            reviewController = new ReviewController(
                    api,
                    this::isLogged,
                    this::hideModal,
                    this::loadMyReservations,
                    this::loadProfile,
                    this::log
            );
        }
        return reviewController;
    }

    @SuppressWarnings("unchecked")
    private void loadProfile() {
        if (!isLogged()) {
            return;
        }
        try {
            Map<String, Object> response = api.getMap("/api/profile/me");
            String previousUserName = currentUserName;
            currentUserName = String.valueOf(response.get("name"));
            currentUserEmail = String.valueOf(response.get("email"));
            sessionLabel.setText("Sesion: " + currentUserName);
            if (previousUserName != null && !previousUserName.equals(currentUserName)) {
                syncCurrentUserNameInLoadedViews();
            }

            if (profileNameField != null) {
                profileNameField.setText(currentUserName);
            }
            if (profileEmailLabel != null) {
                profileEmailLabel.setText(currentUserEmail);
            }
            if (profilePhoneField != null) {
                profilePhoneField.setText(nullableString(response.get("phone")));
            }
            if (profileBioArea != null) {
                profileBioArea.setText(nullableString(response.get("bio")));
            }
            if (profileReputationAvatar != null) {
                profileReputationAvatar.setText(profileInitial(currentUserName));
            }
            if (profileReputationName != null) {
                profileReputationName.setText(currentUserName == null ? "Usuario" : currentUserName);
            }
            if (profileReputationRating != null) {
                updateStarRating(profileReputationRating, response.get("averageRating"), true);
            }
            if (profileReputationStatsBox != null) {
                profileReputationStatsBox.getChildren().setAll(
                        profileMetric("Opiniones", response.get("receivedReviews")),
                        profileMetric("Objetos", response.get("publishedItems")),
                        profileMetric("Alquileres", response.get("rentalsAsOwner"))
                );
            }
            if (profileReviewsBox != null) {
                List<Map<String, Object>> reviewData = (List<Map<String, Object>>) response.get("reviews");
                profileReviewsBox.getChildren().clear();
                if (reviewData.isEmpty()) {
                    Label empty = new Label("Todavia no has recibido opiniones.");
                    empty.getStyleClass().add("empty-state");
                    profileReviewsBox.getChildren().add(empty);
                } else {
                    reviewData.stream()
                            .map(ReviewUiFactory::profileReviewCard)
                            .forEach(profileReviewsBox.getChildren()::add);
                }
            }
            log("Perfil cargado");
        } catch (Exception ex) {
            log("Error al cargar perfil: " + ex.getMessage());
        }
    }

    private void openPublicProfileModal(Long userId) {
        if (!isLogged()) {
            return;
        }
        try {
            Map<String, Object> response = api.getMap("/api/profile/" + userId);
            Node content = PublicProfileModalFactory.create(response, userId, this::reportUserButton);
            showModal("Perfil publico", "Las valoraciones se publican tras completar un alquiler.", content, "modal-card", 640, 500);
        } catch (Exception ex) {
            log("Error al abrir perfil publico: " + ex.getMessage());
        }
    }

    private String profileInitial(String name) {
        return name == null || name.isBlank() ? "?" : name.substring(0, 1).toUpperCase();
    }

    private void updateProfile() {
        if (!isLogged()) {
            return;
        }
        try {
            Map<String, Object> response = api.put("/api/profile/me", Map.of(
                    "name", profileNameField.getText(),
                    "phone", profilePhoneField.getText(),
                    "bio", profileBioArea.getText()
            ));
            String previousUserName = currentUserName;
            currentUserName = String.valueOf(response.get("name"));
            currentUserEmail = String.valueOf(response.get("email"));
            sessionLabel.setText("Sesion: " + currentUserName);
            if (previousUserName == null || !previousUserName.equals(currentUserName)) {
                syncCurrentUserNameInLoadedViews();
            }
            log("Perfil actualizado");
            loadProfile();
        } catch (Exception ex) {
            log("Error al actualizar perfil: " + ex.getMessage());
        }
    }

    private void changePassword() {
        if (!isLogged()) {
            return;
        }
        String currentPassword = profileCurrentPasswordField.getText();
        String newPassword = profileNewPasswordField.getText();
        String confirmPassword = profileConfirmPasswordField.getText();
        if (currentPassword == null || currentPassword.isBlank()
                || newPassword == null || newPassword.isBlank()
                || confirmPassword == null || confirmPassword.isBlank()) {
            log("Completa todos los campos de contrasena.");
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            log("La nueva contrasena y la confirmacion no coinciden.");
            return;
        }
        try {
            api.put("/api/profile/me/password", Map.of(
                    "currentPassword", currentPassword,
                    "newPassword", newPassword
            ));
            profileCurrentPasswordField.clear();
            profileNewPasswordField.clear();
            profileConfirmPasswordField.clear();
            log("Contrasena actualizada correctamente");
        } catch (Exception ex) {
            log("Error al cambiar contrasena: " + ex.getMessage());
        }
    }

    private void submitReport(String type, Long itemId, Long userId, String reason, String details) {
        reportController().submit(type, itemId, userId, reason, details);
    }

    private ReportController reportController() {
        if (reportController == null) {
            reportController = new ReportController(
                    api,
                    this::isLogged,
                    this::hideModal,
                    this::loadNotifications,
                    this::log
            );
        }
        return reportController;
    }

    private void loadAdminOverview() {
        if (adminOverview != null) {
            adminOverview.loadOverview();
        }
    }

    private boolean isLogged() {
        if (currentUserId == null) {
            log("Necesitas iniciar sesion primero");
            return false;
        }
        return true;
    }

    private boolean isAdmin() {
        if (!isLogged()) {
            return false;
        }
        if (!isAdminRole()) {
            log("Se requiere rol ADMIN");
            return false;
        }
        return true;
    }

    private boolean isAdminRole() {
        return "ADMIN".equalsIgnoreCase(currentUserRole);
    }

    private void renderCatalogCards() {
        if (catalogCardsPane == null) {
            return;
        }
        catalogCardsPane.getChildren().clear();
        for (ItemRow item : catalogItems) {
            catalogCardsPane.getChildren().add(buildItemCard(item, false));
        }
        ResponsiveCardGrid.applyCardWidths(catalogCardsPane, catalogCardsPane.getWidth());
    }

    private void renderMyItemsCards() {
        if (myItemsCardsPane == null) {
            return;
        }
        myItemsCardsPane.getChildren().clear();
        for (ItemRow item : myItems) {
            myItemsCardsPane.getChildren().add(buildItemCard(item, true));
        }
        ResponsiveCardGrid.applyCardWidths(myItemsCardsPane, myItemsCardsPane.getWidth());
    }

    private void syncCardGridWidth(FlowPane pane, double viewportWidth) {
        ResponsiveCardGrid.syncWidth(pane, viewportWidth);
    }

    private VBox buildItemCard(ItemRow item, boolean editable) {
        return ItemCardFactory.buildItemCard(item, editable, imageCache, selected -> {
            fillEditForm(selected);
            openEditItemModal();
        }, this::openCatalogDetailModal);
    }

    private void syncCurrentUserNameInLoadedViews() {
        if (currentUserId == null || currentUserName == null) {
            return;
        }
        syncOwnerName(catalogItems);
        syncOwnerName(myItems);
        syncReservationRows();
        syncConversationRows();
        renderCatalogCards();
        renderMyItemsCards();
        if (reservationListView != null) {
            reservationListView.refresh();
        }
        syncConversationRows();
    }

    private void syncOwnerName(ObservableList<ItemRow> items) {
        for (int i = 0; i < items.size(); i++) {
            ItemRow item = items.get(i);
            if (currentUserId.equals(item.ownerId())) {
                items.set(i, item.withOwnerName(currentUserName));
            }
        }
    }

    private void syncReservationRows() {
        if (reservationController != null && currentUserId != null && currentUserName != null) {
            reservationController.syncCurrentUserName(currentUserId, currentUserName);
        }
    }

    private void syncConversationRows() {
        if (chatController != null && currentUserId != null && currentUserName != null) {
            chatController.syncCurrentUserName(currentUserId, currentUserName);
        }
    }

    private Long extractLeadingId(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        int start = text.indexOf('#');
        if (start < 0) {
            return null;
        }
        int end = text.indexOf(" |", start);
        String raw = end > start + 1 ? text.substring(start + 1, end) : text.substring(start + 1).trim();
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private void log(String message) {
        System.out.println(message);
        if (message != null && !message.isBlank() && shouldShowUserFeedback(message)) {
            if (Platform.isFxApplicationThread()) {
                showToast(message);
            } else {
                Platform.runLater(() -> showToast(message));
            }
        }
    }

    private void showToast(String message) {
        if (toastLabel == null || message == null || message.isBlank()) {
            return;
        }
        toastLabel.setText(userFeedbackMessage(message));
        toastLabel.setVisible(true);
        toastLabel.setManaged(true);
        toastLabel.toFront();
        if (toastHideTransition != null) {
            toastHideTransition.stop();
        }
        toastHideTransition = new PauseTransition(Duration.seconds(3));
        toastHideTransition.setOnFinished(e -> {
            toastLabel.setVisible(false);
            toastLabel.setManaged(false);
        });
        toastHideTransition.playFromStart();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

