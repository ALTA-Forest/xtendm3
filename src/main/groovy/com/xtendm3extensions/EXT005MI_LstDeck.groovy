// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to list deck profiles from EXTDPH
// Transaction LstDeck
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

// Date         Changed By                         Description
// 2023-04-10   Jessica Bjorklund (Columbus)       Creation
// 2024-10-17   Jessica Bjorklund (Columbus)       Allow blank input for Strings
// 2024-10-28   Jessica Bjorklund (Columbus)       Change output field DPNA to 100 long



/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DPNA - Deck Name
 * @param: SORT - Sort Code
 * @param: YARD - Yard
 * 
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: DIVI - Division
 * @return: DPID - Deck ID
 * @return: DPNA - Deck Name
 * @return: TYPE - Deck Type
 * @return: SORT - Sort Code
 * @return: MBFW - Weight of 1 MBF
 * @return: YARD - Yard
 * @return: DPDT - Deck Date
 * @return: DPLC - Life Cycle
 * 
*/


public class LstDeck extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database  
  private final ProgramAPI program
  private final LoggerAPI logger
  
  Integer inCONO
  String inDIVI
  int inDPID
  String inTYPE
  String inSORT
  String inYARD
  int numberOfFields

  
  // Constructor 
  public LstDeck(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger) {
     this.mi = mi
     this.database = database 
     this.program = program
     this.logger = logger
  } 
    
  public void main() { 
     // Set Company Number
     inCONO = mi.in.get("CONO")      
     if (inCONO == null || inCONO == 0) {
        inCONO = program.LDAZD.CONO as Integer
     } 

     // Set Division
     if (mi.in.get("DIVI") != null) {
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

     // Deck Type
     if (mi.in.get("TYPE") != null) {
        inTYPE = mi.inData.get("TYPE").trim() 
     } else {
        inTYPE = ""     
     }

     // Sort Code
     if (mi.in.get("SORT") != null) {
        inSORT = mi.inData.get("SORT").trim() 
     } else {
        inSORT = ""   
     }

     // Yard
     if (mi.in.get("YARD") != null) {
        inYARD = mi.inData.get("YARD").trim() 
     } else {
        inYARD = ""   
     }


     // List deck profile
     listDeckProfile()
  }

  //******************************************************************** 
  // List Deck Profiles from EXTDPH
  //******************************************************************** 
  void listDeckProfile(){ 
     ExpressionFactory expression = database.getExpressionFactory("EXTDPH")

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

     if (inTYPE != "") {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXTYPE", inTYPE))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXTYPE", inTYPE)
         numberOfFields = 1
       }
     }

     if (inSORT != "") {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXSORT", inSORT))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXSORT", inSORT)
         numberOfFields = 1
       }
     }

     if (inYARD != "") {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXYARD", inYARD))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXYARD", inYARD)
         numberOfFields = 1
       }
     }


     DBAction actionline = database.table("EXTDPH").index("00").matching(expression).selectAllFields().build()
	   DBContainer line = actionline.getContainer()   

     line.set("EXCONO", inCONO)
     
     int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()               
     actionline.readAll(line, 1, pageSize, releasedLineProcessor)               
  }


    Closure<?> releasedLineProcessor = { DBContainer line -> 
      mi.outData.put("CONO", line.get("EXCONO").toString())
      mi.outData.put("DIVI", line.getString("EXDIVI"))
      mi.outData.put("DPID", line.get("EXDPID").toString()) 
      mi.outData.put("DPNA", line.getString("EXDPNA")) 
      mi.outData.put("TYPE", line.getString("EXTYPE")) 
      mi.outData.put("SORT", line.getString("EXSORT")) 
      mi.outData.put("YARD", line.getString("EXYARD")) 
      mi.outData.put("MBFW", line.getDouble("EXMBFW").toString()) 
      mi.outData.put("DPDT", line.get("EXDPDT").toString()) 
      mi.outData.put("DPLC", line.get("EXDPLC").toString()) 
      mi.write() 
   } 
   
}