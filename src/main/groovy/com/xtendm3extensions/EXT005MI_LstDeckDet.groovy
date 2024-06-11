// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to list deck profile details from EXTDPD
// Transaction LstDeckDet
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DPID - Deck ID
 * 
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: DIVI - Division
 * @return: DPID - Deck ID
 * @return: LOAD - Current Loads
 * @return: DLOG - Current Logs
 * @return: GVBF - Gross Volume
 * @return: NVBF - Net Volume
 * @return: ESWT - Estimated Weight
 * @return: TCOI - Cost of Inventory
 * @return: AWBF - Average Weight of 1 Mbf
 * @return: GBFL - Average Gross bf/Log
 * @return: NBFL - Average Net bf/Log
 * 
*/


public class LstDeckDet extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database  
  private final ProgramAPI program
  
  Integer inCONO
  String inDIVI
  int inDPID
  int numberOfFields

  
  // Constructor 
  public LstDeckDet(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
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
     

     // List deck details
     listDeckDetails()
  }

  //******************************************************************** 
  // List Deck Details from EXTDPD
  //******************************************************************** 
  void listDeckDetails(){ 
     ExpressionFactory expression = database.getExpressionFactory("EXTDPD")

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


     DBAction actionline = database.table("EXTDPD").index("00").matching(expression).selectAllFields().build()
	 DBContainer line = actionline.getContainer()   

     line.set("EXCONO", inCONO)
     
     int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()              
     actionline.readAll(line, 1, pageSize, releasedLineProcessor)               
  }


    Closure<?> releasedLineProcessor = { DBContainer line -> 
      mi.outData.put("CONO", line.get("EXCONO").toString())
      mi.outData.put("DIVI", line.getString("EXDIVI"))
      mi.outData.put("DPID", line.get("EXDPID").toString()) 
      mi.outData.put("LOAD", line.getDouble("EXLOAD").toString()) 
      mi.outData.put("DLOG", line.getDouble("EXDLOG").toString()) 
      mi.outData.put("GVBF", line.getDouble("EXGVBF").toString()) 
      mi.outData.put("NVBF", line.getDouble("EXNVBF").toString()) 
      mi.outData.put("ESWT", line.getDouble("EXESWT").toString()) 
      mi.outData.put("TCOI", line.getDouble("EXCOST").toString()) 
      mi.outData.put("AWBF", line.getDouble("EXAWBF").toString()) 
      mi.outData.put("GBFL", line.getDouble("EXGBFL").toString()) 
      mi.outData.put("NBFL", line.getDouble("EXNBFL").toString()) 
      mi.write() 
   } 
   
}