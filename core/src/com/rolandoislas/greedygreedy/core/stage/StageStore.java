package com.rolandoislas.greedygreedy.core.stage;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.pay.PurchaseSystem;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.rolandoislas.greedygreedy.core.GreedyClient;
import com.rolandoislas.greedygreedy.core.actor.Chip;
import com.rolandoislas.greedygreedy.core.actor.IconChooser;
import com.rolandoislas.greedygreedy.core.data.Constants;
import com.rolandoislas.greedygreedy.core.data.GreedyPurchaseManagerConfig;
import com.rolandoislas.greedygreedy.core.data.Icon;
import com.rolandoislas.greedygreedy.core.data.Purchase;
import com.rolandoislas.greedygreedy.core.event.DialogCallbackHandler;
import com.rolandoislas.greedygreedy.core.event.IconChooseListener;
import com.rolandoislas.greedygreedy.core.net.GreedyApi;
import com.rolandoislas.greedygreedy.core.ui.CallbackDialog;
import com.rolandoislas.greedygreedy.core.ui.skin.DialogSkin;
import com.rolandoislas.greedygreedy.core.util.GreedyException;
import com.rolandoislas.greedygreedy.core.util.Logger;
import com.rolandoislas.greedygreedy.core.util.PreferencesUtil;
import com.rolandoislas.greedygreedy.core.util.TextUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class StageStore extends Stage implements DialogCallbackHandler {
    private final IconChooser iconChooser;
    private final Label labelPoints;
    private final DialogSkin dialogSkin;
    private final Chip chip;
    private ArrayList<Chip> chipsIab;
    private CallbackDialog messageDialog;

    public StageStore(String message) {
        // Title
        Label.LabelStyle ls = new Label.LabelStyle();
        ls.font = TextUtil.generateScaledFont(1.25f);
        Label title = new Label(Constants.NAME, ls);
        title.setPosition(getWidth() / 2 - title.getWidth() / 2, getHeight() - title.getHeight());
        addActor(title);
        // Home button
        TextButton.TextButtonStyle tbs = new TextButton.TextButtonStyle();
        tbs.font = TextUtil.generateScaledFont(1f);
        final TextButton startGame = new TextButton("Main Menu", tbs);
        startGame.setPosition(getWidth() / 2 - startGame.getWidth() / 2, 0);
        startGame.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                GreedyClient.setStage(new StageMenu());
            }
        });
        addActor(startGame);
        // Icon label
        float margin = getHeight() * .05f;
        Label.LabelStyle lbs = new Label.LabelStyle();
        lbs.font = TextUtil.generateScaledFont(0.5f);
        Label labelIcon = new Label("Icons", lbs);
        labelIcon.setPosition(getWidth() * 0.025f, title.getY() - labelIcon.getHeight() - margin);
        addActor(labelIcon);
        // Icon chooser
        iconChooser = new IconChooser();
        iconChooser.setSize(getWidth(), getHeight() * .15f);
        iconChooser.setPosition(0, labelIcon.getY() - iconChooser.getHeight());
        iconChooser.addListener(new IconChooseListener(){
            @Override
            public void itemSelected(int id, Purchase.Type type, int price, boolean owned) {
                StageStore.this.itemSelected(id, type, price, owned);
            }
        });
        addActor(iconChooser);
        // Chip in app purchases
        this.dialogSkin = new DialogSkin();
        this.chipsIab = new ArrayList<Chip>();
        if (PurchaseSystem.hasManager() && !Constants.FORCE_DEV_API) {
            // Chip IAB icon label
            Label labelChipIab = new Label("Chips", lbs);
            labelChipIab.setPosition(labelIcon.getX(), iconChooser.getY() - labelChipIab.getHeight() - margin);
            addActor(labelChipIab);
            // Chip IAB icons
            final HashMap<String, String> chipIabs = new HashMap<String, String>();
            chipIabs.put(GreedyPurchaseManagerConfig.PURCHASE_POINTS_200, "200");
            chipIabs.put(GreedyPurchaseManagerConfig.PURCHASE_POINTS_600, "600");
            chipIabs.put(GreedyPurchaseManagerConfig.PURCHASE_POINTS_1000, "1000");
            float size = getWidth() * .25f;
            float x = (getWidth() - size) / 2 - size;
            float y = labelChipIab.getY() - size;
            int iabIndex = 0;
            for (final HashMap.Entry<String, String> chipIabEntry : chipIabs.entrySet()) {
                Chip chipIab = new Chip();
                chipIab.setSize(size, size);
                chipIab.setPosition(x, y);
                chipIab.setColor(Color.BLACK);
                Label labelIab = new Label(chipIabEntry.getValue(), lbs);
                labelIab.setPosition(chipIab.getX() + chipIab.getWidth() / 2 - labelIab.getWidth() / 2,
                        chipIab.getY() + chipIab.getHeight() / 2 - labelIab.getHeight() / 2);
                ClickListener clickListener = new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        PurchaseSystem.purchase(chipIabEntry.getKey());
                    }
                };
                labelIab.addListener(clickListener);
                chipIab.addListener(clickListener);
                addActor(chipIab);
                addActor(labelIab);
                this.chipsIab.add(chipIab);
                x += size;
                // Add restore purchases button
                if (iabIndex == chipIabs.size() - 1) {
                    Label restoreButton = new Label("Restore Purchases", lbs);
                    restoreButton.setPosition((getWidth() - restoreButton.getWidth()) / 2,
                            chipIab.getY() - restoreButton.getHeight() - margin);
                    final StageStore that = this;
                    restoreButton.addListener(new ClickListener(){
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            CallbackDialog dialog = new CallbackDialog("Restoring purchases", dialogSkin);
                            dialog.show(that);
                            PurchaseSystem.purchaseRestore();
                        }
                    });
                    addActor(restoreButton);
                }
                iabIndex++;
            }
        }
        // Points chip
        chip = new Chip();
        chip.setSize(getWidth() * 0.1f, getWidth() * 0.1f);
        chip.setPosition(startGame.getX() + startGame.getWidth() / 2 - chip.getWidth(),
                startGame.getY() + chip.getHeight() * 1.25f);
        chip.setColor(Color.BLACK);
        addActor(chip);
        // Points label
        labelPoints = new Label(String.valueOf(PreferencesUtil.getPoints()), lbs);
        labelPoints.setPosition(chip.getX() + chip.getWidth() * 1.25f, chip.getY() +
                (chip.getHeight() - labelPoints.getHeight()) / 2);
        labelPoints.setText(String.valueOf(PreferencesUtil.getPoints()));
        addActor(labelPoints);
        // Message
        messageDialog = null;
        if (message != null && !message.isEmpty()) {
            messageDialog = new CallbackDialog(message, dialogSkin);
            messageDialog.button("Confirm");
            messageDialog.show(this);
        }
    }

    public StageStore() {
        this("");
    }

    @Override
    public void dispose() {
        super.dispose();
        dialogSkin.dispose();
        chip.dispose();
        for (Chip chip : chipsIab)
            chip.dispose();
    }

    private void itemSelected(int id, Purchase.Type type, int price, boolean owned) {
        if (owned) {
            if (type.equals(Purchase.Type.ICON)) {
                try {
                    GreedyApi.putIcon(id);
                    iconChooser.setSelection(id);
                } catch (GreedyException e) {
                    Logger.exception(e);
                    GreedyClient.setStage(new StageMenu("API connection error"));
                }
            }
        }
        else {
            if (type.equals(Purchase.Type.ICON)) {
                CallbackDialog dialog = new CallbackDialog("Purchase Icon", dialogSkin);
                dialog.text(String.format(Locale.US, "Purchase icon %s for %d chips?",
                        Icon.values()[id].name(), price));
                dialog.button("Confirm", new int[]{id, type.ordinal()});
                dialog.button("Cancel");
                dialog.show(this);
            }
        }
    }

    @Override
    public void onBackButtonPressed() {
        if (messageDialog != null)
            messageDialog.hide();
        else
            GreedyClient.setStage(new StageMenu());
    }

    @Override
    public Color getBackgroundColor() {
        return Color.SKY;
    }

    @Override
    public void dialogResult(Object object) {
        if (object == null)
            return;
        if (object instanceof int[]) {
            int[] purchase = (int[]) object;
            int id = purchase[0];
            int type = purchase[1];
            if (type == Purchase.Type.ICON.ordinal()) {
                try {
                    GreedyApi.purchase(id, type);
                    iconChooser.unlock(id);
                    GreedyApi.putIcon(id);
                    iconChooser.setSelection(id);
                    updatePoints();
                } catch (GreedyException e) {
                    Logger.exception(e);
                    if (e.getMessage().equalsIgnoreCase("Not enough points to purchase")) {
                        CallbackDialog dialog = new CallbackDialog("Purchase Fail", dialogSkin);
                        dialog.text("Not enough points to purchase.");
                        dialog.button("Close");
                        dialog.show(this);
                        updatePoints();
                        return;
                    }
                    GreedyClient.setStage(new StageMenu("API connection error"));
                }
            }
        }
    }

    private void updatePoints() {
        Thread updatePointsThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    PreferencesUtil.setPoints(GreedyApi.getPoints());
                } catch (GreedyException ee) {
                    Logger.exception(ee);
                }
                labelPoints.setText(String.valueOf(PreferencesUtil.getPoints()));
            }
        });
        updatePointsThread.setDaemon(true);
        updatePointsThread.setName("Update points");
        updatePointsThread.start();
    }
}
