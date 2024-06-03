// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-08-10
// @version   1.0 
//
// Description 
// This API is to list batch numbers from EXTDBH
// Transaction LstBatch
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DBNO - Batch Number
 * @param: DBTP - Batch Type
 * @param: DBBU - Business Unit
 * @param: STAT - Status
 * 
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: DIVI - Division
 * @return: DBNO - Batch Number
 * @return: DBTP - Batch Type
 * @return: DBBU - Business Unit
 * @return: BUNA - Business Unit Name
 * @return: BDEL - Deliveries
 * @return: BTOT - Total
 * @return: STAT - Status
 * @return: RGDT - Entry Date
 * @return: CHID - User
 * @return: NOTE - Note
 * 
*/

public class LstBatch extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database  
  private final ProgramAPI program
  
  Integer inCONO
  String inDIVI
  int inDBNO
  int inDBTP
  String inDBBU
  int inSTAT
  int numberOfFields
  
  // Constructor 
  public LstBatch(MIAPI mi, DatabaseAPI database,ProgramAPI program) {
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

     // Batch Type
     if (mi.in.get("DBTP") != null) {
        inDBTP = mi.in.get("DBTP") 
     } else {
        inDBTP = 0      
     }

     // Business Unit
     if (mi.in.get("DBBU") != null && mi.in.get("DBBU") != "") {
        inDBBU = mi.inData.get("DBBU").trim() 
     } else {
        inDBBU = ""      
     }

     // Status
     if (mi.in.get("STAT") != null) {
        inSTAT = mi.in.get("STAT")
     } else {
        inSTAT = 0     
     }


     // List Batch Orders from EXTDBH
     listBatchOrders()
  }
 
  //******************************************************************** 
  // List Batch Orders from EXTDBH
  //******************************************************************** 
  void listBatchOrders(){ 
     ExpressionFactory expression = database.getExpressionFactory("EXTDBH")

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

     if (inDBTP != 0) {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXDBTP", String.valueOf(inDBTP)))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXDBTP", String.valueOf(inDBTP))
         numberOfFields = 1
       }
     }

     if (inDBBU != "") {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXDBBU", inDBBU))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXDBBU", inDBBU)
         numberOfFields = 1
       }
     }

     if (inSTAT != 0) {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXSTAT", String.valueOf(inSTAT)))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXSTAT", String.valueOf(inSTAT))
         numberOfFields = 1
       }
     }


     DBAction actionline = database.table("EXTDBH").index("00").matching(expression).selectAllFields().build()
	   DBContainer line = actionline.getContainer()   
     
     line.set("EXCONO", inCONO)

     int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()                 
     actionline.readAll(line, 1, pageSize, releasedLineProcessor)               
   } 

    Closure<?> releasedLineProcessor = { DBContainer line -> 
      mi.outData.put("CONO", line.get("EXCONO").toString())
      mi.outData.put("DIVI", line.getString("EXDIVI"))
      mi.outData.put("DBNO", line.get("EXDBNO").toString())
      mi.outData.put("DBTP", line.get("EXDBTP").toString())
      mi.outData.put("DBBU", line.getString("EXDBBU"))
      mi.outData.put("BUNA", line.getString("EXBUNA"))
      mi.outData.put("BDEL", line.get("EXBDEL").toString())
      mi.outData.put("BTOT", line.get("EXBTOT").toString())
      mi.outData.put("STAT", line.get("EXSTAT").toString())
      mi.outData.put("RGDT", line.get("EXRGDT").toString())
      mi.outData.put("CHID", line.getString("EXCHID"))
      mi.outData.put("NOTE", line.getString("EXNOTE"))
      mi.write() 
   } 
   
}