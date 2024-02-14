// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to get a reason from EXTIRP
// Transaction GetReason
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: RPID - Reason ID
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
 * 
*/



public class GetReason extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database 
  private final ProgramAPI program
  
  Integer inCONO
  String inDIVI
  int inRPID
  
  // Constructor 
  public GetReason(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
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
     inDIVI = mi.in.get("DIVI")
     if (inDIVI == null || inDIVI == "") {
        inDIVI = program.LDAZD.DIVI
     }

     // Reason ID
     if (mi.in.get("RPID") != null) {
        inRPID = mi.in.get("RPID") 
     } else {
        inRPID = 0         
     }
     
     // Get record
     getRecord()
  }
 
 //******************************************************************** 
 //Get EXTIRP record
 //********************************************************************     
  void getRecord(){      
     DBAction action = database.table("EXTIRP").index("00").selectAllFields().build()
     DBContainer EXTIRP = action.getContainer()
     EXTIRP.set("EXCONO", inCONO)
     EXTIRP.set("EXDIVI", inDIVI)
     EXTIRP.set("EXRPID", inRPID)
     
    // Read  
    if (action.read(EXTIRP)) {       
      mi.outData.put("CONO", EXTIRP.get("EXCONO").toString())
      mi.outData.put("DIVI", EXTIRP.getString("EXDIVI"))
      mi.outData.put("RPID", EXTIRP.get("EXRPID").toString()) 
      mi.outData.put("RPNA", EXTIRP.getString("EXRPNA")) 
      mi.outData.put("RECD", EXTIRP.getString("EXRECD")) 
      mi.outData.put("ISPC", EXTIRP.get("EXISPC").toString()) 
      mi.outData.put("LOAD", EXTIRP.getDouble("EXLOAD").toString()) 
      mi.outData.put("DLOG", EXTIRP.getDouble("EXDLOG").toString()) 
      mi.outData.put("GVBF", EXTIRP.getDouble("EXGVBF").toString()) 
      mi.outData.put("NVBF", EXTIRP.getDouble("EXNVBF").toString()) 
      mi.outData.put("PCTG", EXTIRP.getDouble("EXPCTG").toString()) 
      mi.outData.put("EINV", EXTIRP.get("EXEINV").toString()) 
      mi.outData.put("NOTE", EXTIRP.getString("EXNOTE")) 
      mi.write() 
    } else {
      mi.error("No record found")   
      return 
    }
  }  
  
}