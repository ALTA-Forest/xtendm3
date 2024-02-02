// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-09-10
// @version   1.0 
//
// Description 
// This API is to list contract trips from EXTDTV
// Transaction LstDeliveryVol
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: DIVI - Division
 * @return: DLNO - Delivery Number
 * @return: LOAD - Load
 * @return: VLOG - Log
 * @return: GVBF - Gross Volume
 * @return: NVBF - Net Volume
 * @return: AMNT - Amount
 * 
*/


public class LstDeliveryVol extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database  
  private final ProgramAPI program
  private final LoggerAPI logger
  
  Integer inCONO
  String inDIVI
  int numberOfFields
  
  // Constructor 
  public LstDeliveryVol(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger) {
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
     inDIVI = mi.in.get("DIVI")
     if (inDIVI == null || inDIVI == "") {
        inDIVI = program.LDAZD.DIVI
     }
    

     // List delivery volume from EXTDTV
     listDeliveryVolume()
  }
 
  //******************************************************************** 
  // List Delivery Volume from EXTDTV
  //******************************************************************** 
  void listDeliveryVolume(){ 
     ExpressionFactory expression = database.getExpressionFactory("EXTDTV")

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


     DBAction actionline = database.table("EXTDTV").index("00").matching(expression).selectAllFields().build()
	   DBContainer line = actionline.getContainer()   
     
     int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()        
     actionline.readAll(line, 0, pageSize, releasedLineProcessor)               
   } 

    Closure<?> releasedLineProcessor = { DBContainer line -> 
      mi.outData.put("CONO", line.get("EXCONO").toString())
      mi.outData.put("DIVI", line.getString("EXDIVI"))
      mi.outData.put("DLNO", line.get("EXDLNO").toString())
      mi.outData.put("LOAD", line.get("EXLOAD").toString())
      mi.outData.put("VLOG", line.get("EXVLOG").toString())
      mi.outData.put("GVBF", line.getDouble("EXGVBF").toString())
      mi.outData.put("NVBF", line.getDouble("EXNVBF").toString())
      mi.outData.put("AMNT", line.getDouble("EXAMNT").toString())
      mi.write()
   }
   
}