// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to list deck register from EXTDPR
// Transaction LstDeck
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DPID - Deck ID
 * @param: TRTP - Transaction Type
 * @param: PUNO - Purchase Order Number
 * 
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: DIVI - Division
 * @return: DPID - Deck ID
 * @return: TRNO - Transaction Number
 * @return: SPEC - Species
 * @return: PUNO - Purchase Order Number
 * @return: INBN - Invoice Batch Number
 * @return: TRDT - Transaction Date
 * @return: TRTP - Transaction Type
 * @return: ACCD - Account Code
 * @return: ACNM - Account Name
 * @return: TRRE - Transaction Receipt
 * @return: TRTT - Transaction Ticket
 * @return: LOAD - Load
 * @return: TLOG - Logs
 * @return: TGBF - Gross BF
 * @return: TNBF - Net BF
 * @return: AUWE - Automatic Volume
 * @return: GRWE - Gross Weight
 * @return: TRWE - Tare Weight
 * @return: NEWE - Net Weight
 * @return: DAAM - Amount
 * @return: RPID - Reason ID
 * 
*/

public class LstDeckRegister extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database 
  private final ProgramAPI program
  
  Integer inCONO
  String inDIVI
  int inDPID
  int inTRTP
  String inPUNO
  int numberOfFields

  
  // Constructor 
  public LstDeckRegister(MIAPI mi, DatabaseAPI database,ProgramAPI program) {
     this.mi = mi
     this.database = database 
     this.program = program
  } 
    
  public void main() { 
     // Set Company Number
     if (mi.in.get("CONO") != null) {
        inCONO = mi.in.get("CONO") as Integer
     } else {
        inCONO = 0      
     }

     // Set Division
     if (mi.inData.get("DIVI") != null) {
        inDIVI = mi.inData.get("DIVI").trim() 
     } else {
        inDIVI = ""     
     }
    
     // Deck ID
     if (mi.in.get("DPID") != null) {
        inDPID = mi.in.get("DPID") 
     } else {
        inDPID = 0    
     }
     
     // Transaction Type
     if (mi.in.get("TRTP") != null) {
        inTRTP = mi.in.get("TRTP") 
     } else {
        inTRTP = 0   
     }

     // Purchase Order Number
     if (mi.inData.get("PUNO") != null) {
        inPUNO = mi.inData.get("PUNO").trim() 
     } else {
        inPUNO = ""     
     }


     // List deck register
     listDeckRegister()
  }

  //******************************************************************** 
  // List Deck Register from EXTDPR
  //******************************************************************** 
  void listDeckRegister(){ 
     ExpressionFactory expression = database.getExpressionFactory("EXTDPR")

     numberOfFields = 0

     if (inCONO != 0) {
       numberOfFields = 1
       expression = expression.eq("EXCONO", String.valueOf(inCONO))
     }

     if (inDIVI != "") {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXDIVI", inDIVI))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXDIVI", inDIVI)
         numberOfFields = 1
       }
     }

     if (inDPID != 0) {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXDPID", String.valueOf(inDPID)))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXDPID", String.valueOf(inDPID))
         numberOfFields = 1
       }
     }

     if (inTRTP != 0) {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXTRTP", String.valueOf(inTRTP)))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXTRTP", String.valueOf(inTRTP))
         numberOfFields = 1
       }
     }

     if (inPUNO != "") {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXPUNO", inPUNO))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXPUNO", inPUNO)
         numberOfFields = 1
       }
     }


     DBAction actionline = database.table("EXTDPR").index("00").matching(expression).selectAllFields().build()
	 DBContainer line = actionline.getContainer()   
     
     int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()              
     actionline.readAll(line, 0, pageSize, releasedLineProcessor)               
  }


    Closure<?> releasedLineProcessor = { DBContainer line -> 
      mi.outData.put("CONO", line.get("EXCONO").toString())
      mi.outData.put("DIVI", line.getString("EXDIVI"))
      mi.outData.put("DPID", line.get("EXDPID").toString()) 
      mi.outData.put("TRNO", line.get("EXTRNO").toString()) 
      mi.outData.put("SPEC", line.getString("EXSPEC")) 
      mi.outData.put("PUNO", line.getString("EXPUNO"))
      mi.outData.put("INBN", line.get("EXINBN").toString()) 
      mi.outData.put("TRDT", line.get("EXTRDT").toString()) 
      mi.outData.put("TRTP", line.get("EXTRTP").toString()) 
      mi.outData.put("ACCD", line.getString("EXACCD")) 
      mi.outData.put("ACNM", line.getString("EXACNM")) 
      mi.outData.put("TRRE", line.getString("EXTRRE")) 
      mi.outData.put("TRTT", line.getString("EXTRTT")) 
      mi.outData.put("LOAD", line.get("EXLOAD").toString()) 
      mi.outData.put("TLOG", line.get("EXTLOG").toString()) 
      mi.outData.put("TGBF", line.getDouble("EXTGBF").toString()) 
      mi.outData.put("TNBF", line.getDouble("EXTNBF").toString()) 
      mi.outData.put("TRNB", line.getDouble("EXTRNB").toString()) 
      mi.outData.put("AUWE", line.get("EXAUWE").toString()) 
      mi.outData.put("GRWE", line.getDouble("EXGRWE").toString()) 
      mi.outData.put("TRWE", line.getDouble("EXTRWE").toString()) 
      mi.outData.put("NEWE", line.getDouble("EXNEWE").toString()) 
      mi.outData.put("DAAM", line.getDouble("EXDAAM").toString()) 
      mi.outData.put("RPID", line.getString("EXRPID")) 
      mi.outData.put("NOTE", line.getString("EXNOTE")) 
      mi.write() 
   }
   
}