package com.tfg.rentalplatform.client.ui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.tfg.rentalplatform.client.util.FormatUtils.toLongOrNull;
import static com.tfg.rentalplatform.client.util.StatusText.reportLabel;
import static com.tfg.rentalplatform.client.util.TextUtils.cleanDisplayText;

public final class AdminReportRowFactory {

    private AdminReportRowFactory() {
    }

    public static Node create(
            Map<String, Object> report,
            Consumer<Long> openItemDetail,
            Consumer<Long> openUserProfile,
            BiConsumer<Long, String> openChat,
            ReportActionHandler updateReportAction,
            UserActiveHandler setUserActive
    ) {
        boolean itemReport = "ITEM".equals(String.valueOf(report.get("type")));
        String target = itemReport ? cleanDisplayText(report.get("itemTitle")) : cleanDisplayText(report.get("userName"));
        if (target.isBlank()) {
            target = "Sin objetivo";
        }

        Label typeLabel = new Label(itemReport ? "Producto" : "Usuario");
        typeLabel.getStyleClass().addAll("admin-report-chip", itemReport ? "admin-report-chip-item" : "admin-report-chip-user");
        Label status = new Label(reportLabel(report.get("status")));
        status.getStyleClass().add("admin-report-status");

        Long reportId = toLongOrNull(report.get("id"));
        Long targetId = toLongOrNull(report.get(itemReport ? "itemId" : "userId"));
        Long contactUserId = toLongOrNull(report.get(itemReport ? "itemOwnerId" : "userId"));
        String contactUserName = cleanDisplayText(report.get(itemReport ? "itemOwnerName" : "userName"));
        boolean userActive = Boolean.parseBoolean(String.valueOf(report.getOrDefault("userActive", true)));
        boolean itemModerationBlocked = Boolean.parseBoolean(String.valueOf(report.getOrDefault("itemModerationBlocked", false)));
        boolean open = "OPEN".equals(String.valueOf(report.get("status")));

        Button viewBtn = new Button(itemReport ? "Ver anuncio" : "Ver perfil");
        viewBtn.getStyleClass().add("secondary-btn");
        viewBtn.setDisable(targetId == null);
        viewBtn.setMinWidth(130);
        viewBtn.setOnAction(e -> {
            if (itemReport) {
                openItemDetail.accept(targetId);
            } else {
                openUserProfile.accept(targetId);
            }
        });

        Button contactBtn = new Button("Contactar");
        contactBtn.getStyleClass().add("secondary-btn");
        contactBtn.setDisable(contactUserId == null);
        contactBtn.setMinWidth(120);
        contactBtn.setOnAction(e -> openChat.accept(contactUserId, contactUserName));

        Label title = new Label(target);
        title.getStyleClass().add("admin-report-title");
        title.setWrapText(true);
        title.setMaxWidth(760);
        Label meta = new Label(cleanDisplayText(report.get("reason")) + " - Reportado por " + cleanDisplayText(report.get("reporterName")));
        meta.getStyleClass().add("admin-report-meta");
        meta.setWrapText(true);
        meta.setMaxWidth(760);
        Label details = new Label(cleanDisplayText(report.get("details")));
        details.getStyleClass().add("admin-report-details");
        details.setWrapText(true);
        details.setMaxWidth(760);
        details.setVisible(!details.getText().isBlank());
        details.setManaged(details.isVisible());

        HBox badges = new HBox(8, typeLabel, status);
        VBox copy = new VBox(4, badges, title, meta, details);
        copy.setMaxWidth(780);
        HBox.setHgrow(copy, Priority.ALWAYS);

        Button closeBtn = new Button("Cerrar");
        closeBtn.setMinWidth(130);
        closeBtn.setDisable(!open);
        closeBtn.setOnAction(e -> updateReportAction.update(reportId, "close", "Reporte cerrado"));

        boolean reactivateItemAction = itemReport && itemModerationBlocked;
        boolean unlockAction = !itemReport && !userActive;
        Button actionBtn = new Button(itemReport
                ? reactivateItemAction ? "Reactivar objeto" : "Pausar objeto"
                : unlockAction ? "Desbloquear usuario" : "Bloquear usuario");
        actionBtn.getStyleClass().add(unlockAction || reactivateItemAction ? "secondary-btn" : "danger-btn");
        actionBtn.setMinWidth(unlockAction ? 180 : reactivateItemAction ? 170 : 140);
        actionBtn.setPrefWidth(unlockAction ? 180 : reactivateItemAction ? 170 : 140);
        actionBtn.setDisable(itemReport ? targetId == null || (!open && !reactivateItemAction) : targetId == null || (!open && userActive));
        actionBtn.setOnAction(e -> {
            if (reactivateItemAction) {
                updateReportAction.update(reportId, "reactivate-item", "Objeto reactivado");
            } else if (unlockAction) {
                setUserActive.update(targetId, true, "Usuario desbloqueado");
            } else {
                updateReportAction.update(reportId, itemReport ? "pause-item" : "block-user",
                        itemReport ? "Objeto pausado" : "Usuario bloqueado");
            }
        });

        HBox actions = new HBox(8, viewBtn, contactBtn, closeBtn, actionBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setMinWidth(620);
        actions.setPrefWidth(620);
        actions.setMaxWidth(620);

        HBox row = new HBox(14, copy, actions);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("admin-report-row");
        return row;
    }

    @FunctionalInterface
    public interface ReportActionHandler {
        void update(Long reportId, String action, String successMessage);
    }

    @FunctionalInterface
    public interface UserActiveHandler {
        void update(Long userId, boolean active, String successMessage);
    }
}
