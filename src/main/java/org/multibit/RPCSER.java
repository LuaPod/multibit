package org.multibit;

import java.math.BigInteger;
import java.net.*;
import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import com.google.bitcoin.core.Address;
import com.google.bitcoin.core.AddressFormatException;
import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.core.InsufficientMoneyException;
import com.google.bitcoin.core.ScriptException;
import com.google.bitcoin.core.Sha256Hash;
import com.google.bitcoin.core.StoredBlock;
import com.google.bitcoin.core.Transaction;
import com.google.bitcoin.core.TransactionBroadcaster;
import com.google.bitcoin.core.TransactionInput;
import com.google.bitcoin.core.TransactionOutput;
import com.google.bitcoin.core.Utils;
import com.google.bitcoin.core.Wallet;
import com.google.bitcoin.core.Wallet.SendRequest;
import com.google.bitcoin.core.WrongNetworkException;

import org.multibit.RPCSER;
import org.multibit.controller.Controller;
import org.multibit.controller.bitcoin.BitcoinController;
import org.multibit.controller.core.CoreController;
import org.multibit.controller.exchange.ExchangeController;
import org.multibit.exchange.CurrencyConverter;
import org.multibit.file.BackupManager;
import org.multibit.file.FileHandler;
import org.multibit.file.WalletLoadException;
import org.multibit.message.Message;
import org.multibit.message.MessageManager;
import org.multibit.model.bitcoin.BitcoinModel;
import org.multibit.model.bitcoin.WalletAddressBookData;
import org.multibit.model.bitcoin.WalletData;
import org.multibit.model.bitcoin.WalletInfoData;
import org.multibit.model.core.CoreModel;
import org.multibit.model.exchange.ConnectHttps;
import org.multibit.model.exchange.ExchangeModel;
import org.multibit.network.*;
import org.multibit.platform.GenericApplication;
import org.multibit.platform.GenericApplicationFactory;
import org.multibit.platform.GenericApplicationSpecification;
import org.multibit.platform.listener.GenericOpenURIEvent;
import org.multibit.store.WalletVersionException;
import org.multibit.viewsystem.DisplayHint;
import org.multibit.viewsystem.ViewSystem;
import org.multibit.viewsystem.swing.action.CreateNewReceivingAddressSubmitAction;
import org.multibit.viewsystem.swing.ColorAndFontConstants;
import org.multibit.viewsystem.swing.MultiBitFrame;
import org.multibit.viewsystem.swing.action.ExitAction;
import org.multibit.viewsystem.swing.view.components.FontSizer;
import org.multibit.viewsystem.swing.view.panels.ReceiveBitcoinPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.CharBuffer;
import java.util.*;
import java.util.List;
import java.text.*;

import javafx.util.converter.ByteStringConverter;

import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.crypto.KeyCrypterException;
import com.google.protobuf.ByteString;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class RPCSER implements Runnable {
	static BitcoinController bitcoinController;
	static Socket csocket;
		static String CUser = "";
		static String CPass = "";
		static String CPort = "";
		static String CIP = "";
	   RPCSER(BitcoinController bitcoinController) {
	      this.bitcoinController = bitcoinController;
	   }

	   public static void main(BitcoinController bitcoinController) 
	   throws Exception {

			Properties prop = new Properties();
			InputStream input = null;
			try {

				input = new FileInputStream("rpc.properties");
				prop.load(input);
				CPort = prop.getProperty("rpcport");
				CUser =prop.getProperty("rpcuser");
				CPass = prop.getProperty("rpcpassword");
				CIP = prop.getProperty("rpcallowedip");

			} catch (IOException ex) {
				ex.printStackTrace();
			} finally {
				if (input != null) {
					try {
						input.close();
						OutputStream output = null;
						 
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			OutputStream output = null;
			if(CPort == "" || CUser == "" || CPass == "" || CIP == ""){
				try {
					output = new FileOutputStream("rpc.properties");
					if(CPort == ""){prop.setProperty("rpcport", "3304");}
					if(CUser == ""){prop.setProperty("rpcuser", "justin7674");}
					if(CPass == ""){prop.setProperty("rpcpassword", "MADETHISADDON");}
					if(CIP == ""){prop.setProperty("rpcallowedip", "127.0.0.1");}
					prop.store(output, null);
				} catch (IOException io) {
					io.printStackTrace();
				} finally {
					if (output != null) {
						try {
							output.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
	      ServerSocket ssock = new ServerSocket(Integer.parseInt(CPort));
	      System.out.println("Listening");
	      while (true) {
	         Socket sock = ssock.accept();
	         System.out.println("Connected");
	         new Thread(new RPCCLI(bitcoinController,sock,CUser,CPass,CIP)).start();
	         Runtime.getRuntime().gc();
	      }
	   }

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
	}