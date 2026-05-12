package com.tfg.rentalplatform.client.ui;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.Map;
import java.util.function.Consumer;

import static com.tfg.rentalplatform.client.ui.UiFactory.sectionTitle;

public final class NotificationsViewFactory {

    private NotificationsViewFactory() {
    }

    public static NotificationsViewParts create(
            ObservableList<Map<String, Object>> notifications,
            Consumer<Long> markNotificationRead,
            Runnable markAllNotificationsRead
    ) {
        ListView<Map<String, Object>> listView = new ListView<>(notifications);
        listView.getStyleClass().addAll("data-list", "notification-list");
        listView.setCellFactory(list -> notificationCell(markNotificationRead));
        VBox.setVgrow(listView, Priority.ALWAYS);

        Button markAllButton = new Button("Marcar todas");
        markAllButton.setOnAction(e -> markAllNotificationsRead.run());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox toolbar = new HBox(10, sectionTitle("Actividad reciente"), spacer, markAllButton);
        toolbar.getStyleClass().add("toolbar-row");

        VBox root = new VBox(12, toolbar, listView);
        root.getStyleClass().add("page-panel");
        VBox.setVgrow(root, Priority.ALWAYS);

        return new NotificationsViewParts(root, listView);
    }

    private static ListCell<Map<String, Object>> notificationCell(Consumer<Long> markNotificationRead) {
        return new ListCell<>() {
            {
                addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                    Map<String, Object> notification = getItem();
                    if (notification == null || isEmpty()) {
                        return;
                    }
                    Long notificationId = toLong(notification.get("id"));
                    if (notificationId != null && !Boolean.TRUE.equals(notification.get("read"))) {
                        markNotificationRead.accept(notificationId);
                    }
                    event.consume();
                });
            }

            @Override
            protected void updateItem(Map<String, Object> notification, boolean empty) {
                super.updateItem(notification, empty);
                if (empty || notification == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                setText(null);
                Node row = ActivityRowFactory.notificationRow(notification);
                if (row instanceof Region region) {
                    region.prefWidthProperty().bind(getListView().widthProperty().subtract(22));
                }
                setGraphic(row);
            }
        };
    }

    private static Long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public record NotificationsViewParts(Node root, ListView<Map<String, Object>> listView) {
    }
}
