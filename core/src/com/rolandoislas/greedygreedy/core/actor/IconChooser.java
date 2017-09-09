package com.rolandoislas.greedygreedy.core.actor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Disposable;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.rolandoislas.greedygreedy.core.GreedyClient;
import com.rolandoislas.greedygreedy.core.data.Icon;
import com.rolandoislas.greedygreedy.core.data.Purchase;
import com.rolandoislas.greedygreedy.core.data.Triangle;
import com.rolandoislas.greedygreedy.core.event.IconChooseEvent;
import com.rolandoislas.greedygreedy.core.net.GreedyApi;
import com.rolandoislas.greedygreedy.core.stage.StageMenu;
import com.rolandoislas.greedygreedy.core.util.GreedyException;
import com.rolandoislas.greedygreedy.core.util.IconUtil;
import com.rolandoislas.greedygreedy.core.util.Logger;
import com.rolandoislas.greedygreedy.core.util.PreferencesUtil;

import java.util.ArrayList;
import java.util.List;

public class IconChooser extends Actor implements Disposable {
    private static final int ICON_SELECTED = -3; // Owned and selected
    private static final int ICON_OWNED = -1;
    private static final int ICON_UNKNOWN = -2;
    private final ShapeRenderer shapeRenderer;
    private final Thread storeDataThread;
    private ArrayList<Purchase> purchases;
    private float[] leftButtonSize;
    private float[] rightButtonSize;
    private List<Texture> icons;
    private float iconSize;
    private float iconMargin;
    private float arrowSize;
    private float leftMargin;
    private int iconIndex;
    private int maxIndex;
    private List<Integer> iconsPrice;
    private JsonArray availableItems;
    private int maxIcons;
    private Icon currentSelection;
    private boolean firstAct;

    public IconChooser() {
        shapeRenderer = new ShapeRenderer();
        leftButtonSize = getLeftButtonSize();
        rightButtonSize = getRightButtonSize();
        icons = new ArrayList<Texture>();
        iconsPrice = new ArrayList<Integer>();
        currentSelection = PreferencesUtil.getIcon();
        final IconChooser that = this;
        addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                that.clicked(x, y);
            }
        });
        storeDataThread = new Thread(new Runnable() {
            @Override
            public void run() {
                // Get purchases of user
                String purchasesTemp;
                try {
                    purchasesTemp = GreedyApi.getPurchases();
                } catch (GreedyException e) {
                    Logger.exception(e);
                    fail();
                    return;
                }
                purchases = new Gson().fromJson(purchasesTemp, new TypeToken<ArrayList<Purchase>>(){}.getType());
                if (purchases == null) {
                    fail();
                    return;
                }
                // Get all available items
                try {
                    purchasesTemp = GreedyApi.getAllAvailablePurchases();
                } catch (GreedyException e) {
                    Logger.exception(e);
                    fail();
                    return;
                }
                availableItems = new Gson().fromJson(purchasesTemp, JsonArray.class);
                if (purchases == null)
                    fail();
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        updateIcons();
                    }
                });
            }
        });
        storeDataThread.setName("Store Data Thread");
        storeDataThread.setDaemon(true);
    }

    private void fail() {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                GreedyClient.setStage(new StageMenu("API connection error"));
            }
        });
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (!firstAct) {
            storeDataThread.start();
            firstAct = true;
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.end();
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        // Background
        shapeRenderer.setColor(Color.BLACK.r, Color.BLACK.g, Color.BLACK.b, 0.5f);
        shapeRenderer.rect(getX(), getY(), getWidth(), getHeight());
        // Left and right arrows
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.triangle(leftButtonSize[0], leftButtonSize[1], leftButtonSize[2], leftButtonSize[3], 
                leftButtonSize[4], leftButtonSize[5]);
        shapeRenderer.triangle(rightButtonSize[0], rightButtonSize[1], rightButtonSize[2], rightButtonSize[3],
                rightButtonSize[4], rightButtonSize[5]);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
        batch.begin();
        // Icons
        for (Texture icon : icons) {
            int price = iconsPrice.get(icons.indexOf(icon));
            if (price == ICON_UNKNOWN || price > ICON_OWNED)
                batch.setColor(Color.GRAY);
            else if (price == ICON_SELECTED)
                batch.setColor(Color.GREEN);
            else
                batch.setColor(Color.WHITE);
            batch.draw(icon, getX() + iconSize * icons.indexOf(icon) + arrowSize + leftMargin,
                    getY() + iconMargin, iconSize, iconSize);
        }
    }

    public void clicked(float x, float y) {
        // Check left and right arrows
        Vector2 coords = localToStageCoordinates(new Vector2(x, y));
        if (new Triangle(leftButtonSize).contains(coords.x, coords.y)) {
            if (iconIndex > 0)
                iconIndex--;
            else
                iconIndex = maxIndex;
        }
        else if (new Triangle(rightButtonSize).contains(coords.x, coords.y)) {
            if (iconIndex < maxIndex)
                iconIndex++;
            else
                iconIndex = 0;
        }
        updateIcons();
        // Check icons
        for (Texture icon : icons) {
            float iconX = getX() + iconSize * icons.indexOf(icon) + arrowSize + leftMargin;
            float iconY = getY() + iconMargin;
            if (coords.x >= iconX && coords.x <= iconX + iconSize &&
                    coords.y >= iconY && coords.y <= iconY + iconSize) {
                int iconNum = icons.indexOf(icon) + iconIndex * maxIcons;
                fire(new IconChooseEvent(iconNum, Purchase.Type.ICON, getPrice(iconNum, Purchase.Type.ICON),
                        isOwned(iconNum, Purchase.Type.ICON)));
                break;
            }
        }
    }

    @Override
    protected void sizeChanged() {
        leftButtonSize = getLeftButtonSize();
        rightButtonSize = getRightButtonSize();
        updateIcons();
    }

    private void updateIcons() {
        iconMargin = getHeight() * .1f;
        arrowSize = (Math.abs(leftButtonSize[0] - leftButtonSize[2]) + iconMargin);
        float remainingWidth = getWidth() - arrowSize * 2;
        iconSize = getHeight() - iconMargin * 2;
        maxIcons = (int) Math.floor(remainingWidth / iconSize);
        maxIndex = (int) Math.ceil(Icon.values().length / (float) maxIcons) - 1;
        leftMargin = (remainingWidth - maxIcons * iconSize) / 2;
        icons.clear();
        iconsPrice.clear();
        for (int icon = 0; icon < maxIcons; icon++) {
            int iconFullNum = icon + iconIndex * maxIcons;
            if (iconFullNum < Icon.values().length) {
                Texture texture = new Texture(IconUtil.getIconPath(iconFullNum));
                if (isOwned(iconFullNum, Purchase.Type.ICON)) {
                    if (iconFullNum == currentSelection.ordinal())
                        iconsPrice.add(ICON_SELECTED); // Owned and selected
                    else
                        iconsPrice.add(ICON_OWNED); // Owned
                }
                else
                    iconsPrice.add(getPrice(iconFullNum, Purchase.Type.ICON)); // Not owned
                icons.add(texture);
            }
        }
    }

    /**
     * Returns the price of an item.
     * @param itemId item id
     * @param type purchase type
     * @return -2 if no info is available or price
     */
    private Integer getPrice(int itemId, Purchase.Type type) {
        if (availableItems == null)
            return ICON_UNKNOWN;
        for (JsonElement itemElement : availableItems) {
            JsonObject item = itemElement.getAsJsonObject();
            if (item.get("id").getAsInt() == itemId && item.get("type").getAsInt() == type.ordinal()) {
                if (itemId == Icon.DIE_FIVE.ordinal())
                    return ICON_OWNED;
                else
                    return item.get("price").getAsInt();
            }
        }
        return ICON_UNKNOWN;
    }

    private boolean isOwned(int itemId, Purchase.Type type) {
        if (purchases == null)
            return false;
        for (Purchase purchase : purchases) {
            if (purchase.getItemId() == itemId && purchase.getPurchaseType() == type.ordinal())
                return true;
        }
        return false;
    }

    @Override
    protected void positionChanged() {
        sizeChanged();
    }

    private float[] getRightButtonSize() {
        return getButtonSize(false);
    }

    private float[] getLeftButtonSize() {
        return getButtonSize(true);
    }

    private float[] getButtonSize(boolean left) {
        float[] size = new float[6];
        float x = left ? getX() : getRight();
        float y = getY();
        float margin = getHeight() * .1f;
        if (left)
            size[0] = x + margin;
        else
            size[0] = x - margin;
        size[1] = y + getHeight() / 2;

        if (left)
            size[2] = x + getHeight() / 2;
        else
            size[2] = x - getHeight() / 2;
        size[3] = y + getHeight() - margin;

        if (left)
            size[4] = x + getHeight() / 2;
        else
            size[4] = x - getHeight() / 2;
        size[5] = y + margin;
        return size;
    }

    public void setSelection(int id) {
        currentSelection = Icon.values()[id];
        updateIcons();
    }

    public void unlock(int id) {
        Purchase purchase = new Purchase();
        purchase.setItemId(id);
        purchase.setPurchaseType(Purchase.Type.ICON.ordinal());
        purchases.add(purchase);
        updateIcons();
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        for (Texture icon : icons)
            icon.dispose();
    }
}
