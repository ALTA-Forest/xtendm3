// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-08-10
// @version   1.0 
//
// Description 
// This API is to list batch item from EXTDBC
// Transaction LstBatchCharge
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DBNO - Batch Number
 * @param: PUNO - Purchase Order
 * @param: ITNO - Item Number
 * @param: CASN - Payee Number
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: DIVI - Division
 * @return: DBNO - Batch Number
 * @return: PUNO - Purchase Order
 * @return: ITNO - Item Number
 * @return: CASN - Payee Number
 * @return: SUNM - Payee Name
 * @return: CDSE - Cost Sequence Number
 * @return: SUCM - Cost Element
 * @return: INBN - Invoice Batch Number
 * @return: INDT - Invoice Date
 * @return: INAM - Batch Amount
*/

public class LstBatchCharge extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database  
  private final ProgramAPI program
  
  Integer inCONO
  String inDIVI
  int inDBNO
  String inPUNO
  String inITNO
  String inCASN
  int numberOfFields
  
  // Constructor 
  public LstBatchCharge(MIAPI mi, DatabaseAPI database,ProgramAPI program) {
     this.mi = mi
     this.database = database 
     this.program = program
  } 
    
  public void main() { 
     // Set Company Number
     if (mi.in.get("CONO") != null) {
        inCONO = mi.in.get("CONO") 
     } else {
        inCONO = 0      
     }

     // Set Division
     if (mi.inData.get("DIVI") != null) {
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

     // Purchase Order
     if (mi.inData.get("PUNO") != null) {
        inPUNO = mi.inData.get("PUNO").trim() 
     } else {
        inPUNO = ""      
     }

     // Item Number
     if (mi.inData.get("ITNO") != null) {
        inITNO = mi.inData.get("ITNO").trim() 
     } else {
        inITNO = ""      
     }

     // Payee Number
     if (mi.inData.get("CASN") != null) {
        inCASN = mi.inData.get("CASN").trim() 
     } else {
        inCASN = ""      
     }


     // List Batch Order Charges from EXTDBC
     listBatchOrderCharges()
  }
 
  //******************************************************************** 
  // List Batch Order Charges from EXTDBC
  //******************************************************************** 
  void listBatchOrderCharges(){ 
     ExpressionFactory expression = database.getExpressionFactory("EXTDBC")

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

     if (inPUNO != "") {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXPUNO", inPUNO))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXPUNO", inPUNO)
         numberOfFields = 1
       }
     }

     if (inITNO != "") {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXITNO", inITNO))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXITNO", inITNO)
         numberOfFields = 1
       }
     }

     if (inCASN != "") {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXCASN", inCASN))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXCASN", inCASN)
         numberOfFields = 1
       }
     }


     DBAction actionline = database.table("EXTDBC").index("00").matching(expression).selectAllFields().build()
	   DBContainer line = actionline.getContainer()   
     
     int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()        
     actionline.readAll(line, 0, pageSize, releasedLineProcessor)               
   } 

    Closure<?> releasedLineProcessor = { DBContainer line -> 
      mi.outData.put("CONO", line.get("EXCONO").toString())
      mi.outData.put("DIVI", line.getString("EXDIVI"))
      mi.outData.put("DBNO", line.get("EXDBNO").toString())
      mi.outData.put("PUNO", line.getString("EXPUNO"))
      mi.outData.put("ITNO", line.getString("EXITNO"))
      mi.outData.put("CASN", line.getString("EXCASN"))
      mi.outData.put("SUNM", line.getString("EXSUNM"))
      mi.outData.put("CDSE", line.getString("EXCDSE"))
      mi.outData.put("SUCM", line.getString("EXSUCM"))
      mi.outData.put("INBN", line.get("EXINBN").toString())
      mi.outData.put("INDT", line.get("EXINDT").toString())
      mi.outData.put("INQT", line.get("EXINQT").toString())
      mi.outData.put("INAM", line.get("EXINAM").toString())
      mi.write() 
   }
   
}