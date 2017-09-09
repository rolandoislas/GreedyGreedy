package com.rolandoislas.greedygreedy.core.event;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.pay.PurchaseObserver;
import com.badlogic.gdx.pay.Transaction;
import com.rolandoislas.greedygreedy.core.GreedyClient;
import com.rolandoislas.greedygreedy.core.net.GreedyApi;
import com.rolandoislas.greedygreedy.core.stage.StageLogin;
import com.rolandoislas.greedygreedy.core.stage.StageMenu;
import com.rolandoislas.greedygreedy.core.stage.StageStore;
import com.rolandoislas.greedygreedy.core.util.GreedyException;
import com.rolandoislas.greedygreedy.core.util.Logger;
import com.rolandoislas.greedygreedy.core.util.PreferencesUtil;

import java.util.ArrayList;
import java.util.Arrays;

public class GreedyPurchaseObserver implements PurchaseObserver {
    @Override
    public void handleInstall() {
        Logger.debug("Purchase observer initiated");
    }

    @Override
    public void handleInstallError(Throwable e) {
        Logger.exception(new Exception(e));
    }

    @Override
    public void handleRestore(Transaction[] transactions) {
        Logger.debug("Restoring transactions");
        ArrayList<Transaction> transactionsList = PreferencesUtil.getFailedTransactions();
        transactionsList.addAll(Arrays.asList(transactions));
        transactions = transactionsList.toArray(new Transaction[transactionsList.size()]);
        for (Transaction transaction : transactions) {
            try {
                GreedyApi.purchaseIab(transaction);
            }
            catch (GreedyException e) {
                Logger.exception(e);
                PreferencesUtil.saveFailedTransaction(transaction);
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        GreedyClient.setStage(new StageMenu("Error restoring purchases.\nPlease try again later."));
                    }
                });
                return;
            }
        }
        try {
            PreferencesUtil.setPoints(GreedyApi.getPoints());
        } catch (GreedyException e) {
            Logger.exception(e);
        }
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                GreedyClient.setStage(new StageLogin(new StageStore("Transactions restored"), null));
            }
        });
    }

    @Override
    public void handleRestoreError(Throwable e) {
        Logger.exception(new Exception(e));
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                GreedyClient.setStage(new StageMenu("Error restoring purchases."));
            }
        });
    }

    @Override
    public void handlePurchase(Transaction transaction) {
        Logger.debug("Purchase started");
        try {
            GreedyApi.purchaseIab(transaction);
            PreferencesUtil.setPoints(GreedyApi.getPoints());
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    GreedyClient.setStage(new StageLogin(new StageStore("Purchase successful"), null));
                }
            });
        } catch (GreedyException e) {
            Logger.exception(e);
            PreferencesUtil.saveFailedTransaction(transaction);
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    GreedyClient.setStage(new StageMenu("Error with purchase.\nPlease try restoring purchases."));
                }
            });
        }
    }

    @Override
    public void handlePurchaseError(Throwable e) {
        Logger.exception(new Exception(e));
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                GreedyClient.setStage(new StageMenu("Error with purchase"));
            }
        });
    }

    @Override
    public void handlePurchaseCanceled() {
        Logger.debug("Purchase canceled");
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                GreedyClient.setStage(new StageLogin(new StageStore(), null));
            }
        });
    }
}
