// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-08-10
// @version   1.0 
//
// Description 
// This API is to list batch details from EXTDBD
// Transaction LstBatchDetail
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DBNO - Batch Number
 * 
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: DIVI - Division
 * @return: DBNO - Batch Number
 * @return: DLNO - Delivery Number
 * @return: SUNO - Supplier
 * @return: ITNO - Item Number
 * 
*/

public class LstBatchDetail extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database  
  private final ProgramAPI program
  
  Integer inCONO
  String inDIVI
  int inDBNO
  int numberOfFields
  
  // Constructor 
  public LstBatchDetail(MIAPI mi, DatabaseAPI database,ProgramAPI program) {
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

     // List Batch Order Details from EXTDBD
     listBatchOrderDetails()
  }
 
  //******************************************************************** 
  // List Batch Order Details from EXTDBD
  //******************************************************************** 
  void listBatchOrderDetails(){ 
     ExpressionFactory expression = database.getExpressionFactory("EXTDBD")

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

     DBAction actionline = database.table("EXTDBD").index("00").matching(expression).selectAllFields().build()
	   DBContainer line = actionline.getContainer()   

     line.set("EXCONO", inCONO)
     
     int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()        
     actionline.readAll(line, 1, pageSize, releasedLineProcessor)               
   } 

    Closure<?> releasedLineProcessor = { DBContainer line -> 
      mi.outData.put("CONO", line.get("EXCONO").toString())
      mi.outData.put("DIVI", line.getString("EXDIVI"))
      mi.outData.put("DBNO", line.get("EXDBNO").toString())
      mi.outData.put("DLNO", line.get("EXDLNO").toString())
      mi.outData.put("SUNO", line.getString("EXSUNO"))
      mi.outData.put("ITNO", line.getString("EXITNO"))
      mi.write() 
   } 
   
}