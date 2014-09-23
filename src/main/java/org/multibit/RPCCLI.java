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

import org.multibit.RPCCLI;
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

public class RPCCLI implements Runnable {
	static BitcoinController bitcoinController;
	static Socket csocket;
		String CUser = "";
		String CPass = "";
		String CPort = "";
		String CIP = "";
	   RPCCLI(BitcoinController bitcoinController2,Socket csocket2,String CUser2,String CPass2,String CIP2) {
	      this.csocket = csocket2;
	      this.bitcoinController = bitcoinController2;
	      this.CUser = CUser2;
	      this.CPass = CPass2;
	      this.CIP = CIP2;
	   }
	   public void run() {
		   System.out.println(csocket.getRemoteSocketAddress().toString() );
		  if(csocket.getRemoteSocketAddress().toString().contains(CIP))
		  {
		        NumberFormat formatter = new DecimalFormat("#0.00000000");
		        Socket clientSocket = csocket;     
		        InputStream in = null;
				try {
					in = clientSocket.getInputStream();
				} catch (IOException e2) {
					e2.printStackTrace();
				}
				PrintWriter out = null;
				try {
					out = new PrintWriter(clientSocket.getOutputStream(), true);
				} catch (IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
		        byte[] buffer = new byte[12000]; 
		        int bytesRead;
		        try {
					in.read(buffer);
				} catch (IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
		        
				String Request = new String(buffer);
				String endline = Request.substring(Request.lastIndexOf("\r\n\r\n") + 4);
	        	JSONParser parser = new JSONParser();
	        	JSONObject array = null;
	        	Object obj = null;
	        	try {
	        		obj = parser.parse(endline.trim());
	        		array = (JSONObject)obj;
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					out.println(endline);
				}
	   
	        	Object Method = array.get("method");
	        	Object Params = array.get("params");
	        	Object Username = array.get("username");
	        	Object Password = array.get("password");
	        	String Return = "";
	        	if(Method != null & Params != null & Username != null & Password != null)
	        	{
	        		String Method_s = (String)Method;
	        		JSONArray Params_s = (JSONArray)Params;
	        		String Username_s = (String)Username;
	        		String Password_s = (String)Password;
	        		//This will open our RPC conf ini and read just two lines! The first being the user the second the password.
	        		///This pretty much wraps up our quickconfig.
	        		if(Username_s.compareTo(CUser) == 0 & Password_s.compareTo(CPass) == 0)
	        		{
	        			///////////getnewaddress///////////////
	        			if(Method_s.compareTo("getnewaddress") == 0)
	        			{
	        				WalletData finalPerWalletModelData = bitcoinController.getModel().getActivePerWalletModelData();
	        				Wallet tempwallet = finalPerWalletModelData.getWallet();
	        				if(tempwallet != null){
		        				FileHandler fileHandler = bitcoinController.getFileHandler();
		        				WalletInfoData walletInfo = bitcoinController.getModel().getActiveWalletWalletInfo();
		        				ECKey newKey = new ECKey();
		        				String lastAddressString = newKey.toAddress(bitcoinController.getModel().getNetworkParameters()).toString();
		        			
	        					tempwallet.addKey(newKey);
								finalPerWalletModelData.getWalletInfo().addReceivingAddress(new WalletAddressBookData(Params_s.get(0).toString(), lastAddressString),false);
		        				out.println("{\"result\":\""+ lastAddressString + "\"}");
		        				BackupManager.INSTANCE.backupPerWalletModelData(fileHandler, finalPerWalletModelData);
	        				}else{
	        					out.println("{\"error\":\"Wallet Unavailable\"}");
	        				}
	        				

	        				
	        			}
	        			////////sendtoaddress/////////////
	         			if(Method_s.compareTo("sendtoaddress") == 0)
	         			{
	                        // Create a SendRequest.
	                        Address sendAddressObject = null;
	                        
	                        try {
								sendAddressObject = new Address(bitcoinController.getModel().getNetworkParameters(), Params_s.get(0).toString());
							} catch (AddressFormatException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
	                        if(sendAddressObject != null)
	                        {
		                        SendRequest sendRequest = SendRequest.to(sendAddressObject, Utils.toNanoCoins(Params_s.get(1).toString()));
		                        sendRequest.ensureMinRequiredFee = true;
		                        sendRequest.fee = BigInteger.ZERO;
		                        sendRequest.feePerKb = BigInteger.ONE;
		                        boolean failed = false;
		                        try {
									bitcoinController.getModel().getActiveWallet().completeTx(sendRequest, false);
								
		                        } catch (InsufficientMoneyException e1) {
		                        	out.println("{\"result\":{\"error\":\"Send Failed\"}}");
		                        	failed = true;
								}
	                            if(failed == false){
	                            	Transaction transaction = null;
									try {
										transaction = bitcoinController.getMultiBitService().sendCoins(bitcoinController.getModel().getActivePerWalletModelData(), sendRequest, "");
									} catch (KeyCrypterException e) {
										out.println("{\"result\":{\"error\":\"Send Failed\"}}");
										failed = true;
									} catch (AddressFormatException e) {
										out.println("{\"result\":{\"error\":\"Send Failed\"}}");
										failed = true;
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
		                            if (failed == false & transaction != null) {
		                            	out.println("{\"result\":\""+ transaction.getHashAsString() + "\"}");
		                            }
	                            }
	                        }
	                    }
	         			////////getbalance//////////
	         			if(Method_s.compareTo("getbalance") == 0){
	         				out.println("{\"result\":\""+ bitcoinController.getModel().getActiveWallet().getBalance() + "\"}");
	         			}
	         			////////gettransaction////////////
	         			if(Method_s.compareTo("gettransaction") == 0)
	         			{	
	         				
	         				Sha256Hash TempLookup = null;
	         				try
	         				{
	         					TempLookup = new Sha256Hash(Params_s.get(0).toString());
	         				}
	         				catch(IllegalArgumentException e)
	         				{
	         				}
	         				Transaction T = null;
	         				T = bitcoinController.getModel().getActiveWallet().getTransaction(TempLookup);
	         				if(T != null)
	         				{
	         				
	    					String THash = T.getHashAsString();
	    					List<TransactionOutput> TFee = T.getOutputs();
	    					
	        				for(TransactionOutput TI : TFee)
	        				{
	        						
	        					String TIndex = "";
	        					String TConfirms = "";
	        					String TID = "";
	        					String TBlockhash = "";
	        					String TBlock = "";
	        					String TAddress = "";
	        					String TAmount = "";
	        					String TFees = "";
	        					String TCategory = "";
	        					String TTime = "";
	    						if(TI.isMine(bitcoinController.getModel().getActiveWallet()))
	    						{
	    							TTime = ""+ T.getUpdateTime().getTime()/1000;
	    							TAmount = formatter.format(new Float(TI.getValue().floatValue() / 100000000.0f).doubleValue());
	    							TFees = "0.0001";
	    							TID = T.getHashAsString();
	    							try {
	        							TBlockhash = T.getAppearsInHashes().toString();
	        							TIndex = "" + T.getConfidence().getAppearedAtChainHeight();
	        							TConfirms = "'" + (bitcoinController.getMultiBitService().getChain().getBestChainHeight() -  T.getConfidence().getAppearedAtChainHeight()+ 1) +"'";
	        							} catch(Exception a){}
	    							
	    							TIndex = "" + T.getConfidence().getAppearedAtChainHeight();
	    							TBlock = "" + TI.getParentTransaction().getUpdateTime().getTime()/1000;
	    							
	    							try {
	    								TAddress = TI.getScriptPubKey().getToAddress(T.getParams()).toString();
									} catch (ScriptException e) {
										e.printStackTrace();
									}
	    							
	    							if(T.sent(bitcoinController.getModel().getActiveWallet()))
									{
	    								TCategory = "send";
	    								if(TI.isMine(bitcoinController.getModel().getActiveWallet()))
	    								{
	    									TCategory = "send/receive";
	    								}
									}else{
	    								if(TI.isMine(bitcoinController.getModel().getActiveWallet()))
	    								{
	    									TCategory = "receive";
	    								}
									}
	    							if(!Return.isEmpty())
	    							{
	    								Return = Return + ",";
	    							}
	    							if(T.getValueSentToMe(bitcoinController.getModel().getActiveWallet()).floatValue() > 0)
									{
	    								if(TCategory.compareTo("send/receive") == 0)
	    								{
	    									Return = Return + "{\"fee\" : " + TFees + " , \"amount\" : "+TAmount+" , \"blockindex\" : "+TIndex+" , \"category\": \"send\" , \"confirmations\" : "+TConfirms+" , \"address\" : \""+TAddress+"\" , \"txid\" : \""+TID+"\" , \"blocktime\" : "+TBlock+" , \"blockhash\" : \""+TBlockhash+"\" , \"account\" :\"mena\", \"time\" : "+ TTime +"},";
	    									Return = Return + "{\"fee\" : " + TFees + " , \"amount\" : "+TAmount+" , \"blockindex\" : "+TIndex+" , \"category\": \"receive\" , \"confirmations\" : "+TConfirms+" , \"address\" : \""+TAddress+"\" , \"txid\" : \""+TID+"\" , \"blocktime\" : "+TBlock+" , \"blockhash\" : \""+TBlockhash+"\" , \"account\" :\"mena\", \"time\" :\""+ TTime +"}";
	    									
	    								}else{
	    									Return = Return + "{\"fee\" : " + TFees + " , \"amount\" : "+TAmount+" , \"blockindex\" : "+TIndex+" , \"category\": \""+TCategory+"\" , \"confirmations\" : "+TConfirms+" , \"address\" : \""+TAddress+"\" , \"txid\" : \""+TID+"\" , \"blocktime\" : "+TBlock+" , \"blockhash\" : \""+TBlockhash+"\" , \"account\" :\"mena\" , \"time\" : "+ TTime +"}";
	    								}			
	    							}
	    							if(!Return.isEmpty())
	    							{
	    								//out.println("["+Return+"] ");
	    								out.println("{ \"result\" : ["+Return+"] }");
	    							}
	    						}
	        				}
	         				}else{
	         					out.println("{ \"result\" : \"Invalid Transaction\" }");
	         				}
	         			}
	        			////////listtransactions//////////
	        			if(Method_s.compareTo("listtransactions") == 0)
	        			{
	        				int xi = 0;
	        				List<Transaction> TL = bitcoinController.getModel().getActiveWallet().getRecentTransactions(Integer.parseInt(Params_s.get(0).toString()), false);
	        				String TLastblock = "";
	        				for(Transaction T : TL)
	        				{
	        					String THash = T.getHashAsString();
	        					List<TransactionOutput> TFee = T.getOutputs();
	        					
	            				for(TransactionOutput TI : TFee)
	            				{
	            					String TIndex = "";
	            					String TConfirms = "";
	            					String TID = "";
	            					String TBlockhash = "";
	            					String TBlock = "";
	            					String TAddress = "";
	            					String TAmount = "";
	            					String TFees = "";
	            					String TCategory = "";
	            					String TTime = "";
	        						if(TI.isMine(bitcoinController.getModel().getActiveWallet()))
	        						{
	        							TTime = ""+ T.getUpdateTime().getTime()/1000;
	        							
	        							TAmount = formatter.format(new Float(TI.getValue().floatValue() / 100000000.0f).doubleValue());
	        							TFees = "0.0001";
	        							TID = T.getHashAsString();
	        							try {
	        							TBlockhash = T.getAppearsInHashes().toString();
	        							TIndex = "" + T.getConfidence().getAppearedAtChainHeight();
	        							TConfirms = "'" + (bitcoinController.getMultiBitService().getChain().getBestChainHeight() -  T.getConfidence().getAppearedAtChainHeight()+ 1) +"'";
	        							} catch(Exception a){}
	        							if(TIndex.isEmpty())
	        							{
	        								TIndex = "0";
	        								TBlockhash = "";
	        							}
	        							TBlock = "" + TI.getParentTransaction().getUpdateTime().getTime()/1000;
	        							
	        							if(TLastblock.isEmpty())
	        							{
	        								TLastblock = TBlockhash;
	        							}
	        							try {
	        								TAddress = TI.getScriptPubKey().getToAddress(T.getParams()).toString();
										} catch (ScriptException e) {
											e.printStackTrace();
										}
	        							if(T.sent(bitcoinController.getModel().getActiveWallet()))
	    								{
	        								TCategory = "send";
	        								if(TI.isMine(bitcoinController.getModel().getActiveWallet()))
	        								{
	        									TCategory = "send/receive";
	        								}
	    								}else{
	    									
	        								if(TI.isMine(bitcoinController.getModel().getActiveWallet()))
	        								{
	        									TCategory = "receive";
	        								}
	    								}
	        							if(!Return.isEmpty())
	        							{
	        								
	        								Return = Return + ",";
	        							}
	    								if(TCategory.compareTo("send/receive") == 0)
	    								{
	    									Return = Return + "{\"fee\" : " + TFees + " , \"amount\" : "+TAmount+" , \"blockindex\" : "+TIndex+" , \"category\": \"send\" , \"confirmations\" : "+TConfirms+" , \"address\" : \""+TAddress+"\" , \"txid\" : \""+TID+"\" , \"blocktime\" : "+TBlock+" , \"blockhash\" : \""+TBlockhash+"\" , \"account\" :\"mena\", \"time\" : "+ TTime +"},";
	    									Return = Return + "{\"fee\" : " + TFees + " , \"amount\" : "+TAmount+" , \"blockindex\" : "+TIndex+" , \"category\": \"receive\" , \"confirmations\" : "+TConfirms+" , \"address\" : \""+TAddress+"\" , \"txid\" : \""+TID+"\" , \"blocktime\" : "+TBlock+" , \"blockhash\" : \""+TBlockhash+"\" , \"account\" :\"mena\", \"time\" :\""+ TTime +"}";
	    									
	    								}else{
	    									Return = Return + "{\"fee\" : " + TFees + " , \"amount\" : "+TAmount+" , \"blockindex\" : "+TIndex+" , \"category\": \""+TCategory+"\" , \"confirmations\" : "+TConfirms+" , \"address\" : \""+TAddress+"\" , \"txid\" : \""+TID+"\" , \"blocktime\" : "+TBlock+" , \"blockhash\" : \""+TBlockhash+"\" , \"account\" :\"mena\" , \"time\" : "+ TTime +"}";
	    								}									
	        						}
	            				}
	        				}
	        				out.println("{ \"result\" : [ {\"lastblock\": \"" + TLastblock + "\" , \"transactions\" : ["+ Return + "] }] }");
	        				//out.println(" {\"lastblock\": \"" + TLastblock + "\" , \"transactions\" : ["+ Return + "] }");
	        			}
	        			///////
	        			
	        		}else{
	        			out.println("{\"result\":{\"error\":\"Request Empty\"}}"); //Don't even let the server be specific on what is wrong with the request. We already know it receives everything if it is properly formatted
	        		}
	        		
	        	}else{
	        		out.println("{\"result\":{\"error\":\"Request Empty\"}}");
	        	}
	        	out.close();
	        }
		  try {
			csocket.close();
		} catch (IOException e) {
		}
	   }
	}