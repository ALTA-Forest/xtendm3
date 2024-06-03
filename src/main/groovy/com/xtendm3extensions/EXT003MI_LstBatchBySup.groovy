// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-09-25
// @version   1.0 
//
// Description 
// This API is to list batch info by SUNO from EXTDBI
// Transaction LstBatchBySup
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DBNO - Batch Number
 * @param: SUNO - Supplier
 * 
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: DIVI - Division
 * @return: DBNO - Batch Number
 * @return: SUNO - Supplier
 * @return: SUNM - Supplier Name
 * @return: MSGN - Message Number
 * @return: TREF - Reference
 * @return: ORQT - Quantity
 * @return: BIAM - Amount
 * 
*/

import java.lang.Math
import java.math.BigDecimal
import java.math.RoundingMode 

public class LstBatchBySup extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database  
  private final ProgramAPI program
  
  Integer inCONO
  String inDIVI
  int inDBNO
  String inSUNO
  double orderQty
  BigDecimal sumOrderQty
  double amount
  BigDecimal sumAmount
  int outCONO
  String outDIVI
  int outDBNO
  String outSUNO
  String outSUNM
  String outMSGN
  String outTREF
  int numberOfFields
  
  // Constructor 
  public LstBatchBySup(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
     this.mi = mi
     this.database = database 
     this.program = program
  } 
    
  public void main() { 
     // Set Company Number
     inCONO = mi.in.get("CONO")      
     if (inCONO == null || inCONO == 0) {
        inCONO = program.LDAZD.CONO as Integer
     } 

     // Set Division
     if (mi.in.get("DIVI") != null && mi.in.get("DIVI") != "") {
        inDIVI = mi.inData.get("DIVI").trim() 
     } else {
        inDIVI = ""     
     }

     // Batch Number
     if (mi.in.get("DBNO") != null) {
        inDBNO = mi.in.get("DBNO")
     } else {
        inDBNO = 0  
     }

     // Supplier
     if (mi.in.get("SUNO") != null && mi.in.get("SUNO") != "") {
        inSUNO = mi.inData.get("SUNO").trim() 
     } else {
        inSUNO = ""    
     }

     // List batch info by SUNO
     listBatchBySUNO()
     
     mi.outData.put("CONO", outCONO.toString())
     mi.outData.put("DIVI", outDIVI)
     mi.outData.put("DBNO", outDBNO.toString())
     mi.outData.put("SUNO", outSUNO)
     mi.outData.put("SUNM", outSUNM)
     mi.outData.put("MSGN", outMSGN)
     mi.outData.put("TREF", outTREF)
     double sumQty = sumOrderQty.setScale(2, RoundingMode.HALF_UP)
     mi.outData.put("ORQT", String.valueOf(sumQty))
     double sumAmt = sumAmount.setScale(2, RoundingMode.HALF_UP)
     mi.outData.put("BIAM", String.valueOf(sumAmt))
     mi.write() 
  }
 
  //******************************************************************** 
  // List Batch by SUNO
  //******************************************************************** 
  void listBatchBySUNO(){ 
     ExpressionFactory expression = database.getExpressionFactory("EXTDBI")

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

     if (inDBNO != 0) {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXDBNO", String.valueOf(inDBNO)))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXDBNO", String.valueOf(inDBNO))
         numberOfFields = 1
       }
     }

     if (inSUNO != "") {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXSUNO", inSUNO))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXSUNO", inSUNO)
         numberOfFields = 1
       }
     }
     
     orderQty = 0d
     sumOrderQty = 0d
     amount = 0d
     sumAmount = 0d

     DBAction actionline = database.table("EXTDBI").index("00").matching(expression).selectAllFields().build()
	   DBContainer line = actionline.getContainer()   

     line.set("EXCONO", inCONO)
     
     int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()        
     actionline.readAll(line, 1, pageSize, releasedLineProcessor)               
   } 

    Closure<?> releasedLineProcessor = { DBContainer line -> 
      outCONO = line.get("EXCONO")
      outDIVI = line.getString("EXDIVI")
      outDBNO = line.get("EXDBNO")
      outSUNO = line.getString("EXSUNO")
      outSUNM = line.getString("EXSUNM")
      outMSGN = line.getString("EXMSGN")
      outTREF = line.getString("EXTREF")
      orderQty = line.getDouble("EXORQT")
      amount = line.getDouble("EXBIAM")
      sumOrderQty = sumOrderQty + orderQty
      sumAmount = sumAmount + amount
   } 
   
}