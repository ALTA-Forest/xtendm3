// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to list reason from EXTIRP
// Transaction LstReason
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * 
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: DIVI - Division
 * @return: RPID - Reason ID
 * @return: RPNA - Reason Name
 * @return: RECD - Reason Code
 * @return: ISPC - Is Percentage
 * @return: LOAD - Loads
 * @return: DLOG - Logs
 * @return: GVBF - Gross Volume
 * @return: NVBF - Net Volume
 * @return: PCTG - Percentage
 * @return: EINV - Effect on Inventory
 * @return: NOTE - Note
 * 
*/


public class LstReason extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database  
  private final ProgramAPI program
  
  Integer inCONO
  String inDIVI
  int numberOfFields

  
  // Constructor 
  public LstReason(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
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
    

     // List reasons
     listReasons()
  }

  //******************************************************************** 
  // List Reasons from EXTIRP
  //******************************************************************** 
  void listReasons(){ 
     ExpressionFactory expression = database.getExpressionFactory("EXTIRP")

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


     DBAction actionline = database.table("EXTIRP").index("00").matching(expression).selectAllFields().build()
	   DBContainer line = actionline.getContainer()   
     
     int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()              
     actionline.readAll(line, 0, pageSize, releasedLineProcessor)               
  }


    Closure<?> releasedLineProcessor = { DBContainer line -> 
      mi.outData.put("CONO", line.get("EXCONO").toString())
      mi.outData.put("DIVI", line.getString("EXDIVI"))
      mi.outData.put("RPID", line.get("EXRPID").toString()) 
      mi.outData.put("RPNA", line.getString("EXRPNA")) 
      mi.outData.put("RECD", line.getString("EXRECD")) 
      mi.outData.put("ISPC", line.get("EXISPC").toString()) 
      mi.outData.put("LOAD", line.getDouble("EXLOAD").toString()) 
      mi.outData.put("DLOG", line.getDouble("EXDLOG").toString()) 
      mi.outData.put("GVBF", line.getDouble("EXGVBF").toString()) 
      mi.outData.put("NVBF", line.getDouble("EXNVBF").toString()) 
      mi.outData.put("PCTG", line.getDouble("EXPCTG").toString()) 
      mi.outData.put("EINV", line.get("EXEINV").toString()) 
      mi.outData.put("NOTE", line.getString("EXNOTE")) 
      mi.write() 
   } 
   
}