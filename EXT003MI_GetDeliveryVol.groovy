// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-09-10
// @version   1.0 
//
// Description 
// This API is to get a contract brand from EXTDTV
// Transaction GetDeliveryVol
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DLNO - Delivery Number
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: DIVI - Division
 * @return: DLNO - Delivery Number
 * @return: LOAD - Load
 * @return: VLOG - Log
 * @return: GVBF - Gross Volume BF
 * @return: NVBF - Net Volume BF
 * 
*/



public class GetDeliveryVol extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database 
  private final ProgramAPI program
  private final LoggerAPI logger
  
  Integer inCONO
  String inDIVI
  int inDLNO
  
  // Constructor 
  public GetDeliveryVol(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger) {
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

     // Delivery Number
     if (mi.in.get("DLNO") != null) {
        inDLNO = mi.in.get("DLNO") 
     } else {
        inDLNO = 0      
     }
     
     
     // Get record
     getRecord()
  }
 
 //******************************************************************** 
 //Get EXTDTV record
 //********************************************************************     
  void getRecord(){      
     DBAction action = database.table("EXTDTV").index("00").selectAllFields().build()
     DBContainer EXTDTV = action.getContainer()
     EXTDTV.set("EXCONO", inCONO)
     EXTDTV.set("EXDIVI", inDIVI)
     EXTDTV.set("EXDLNO", inDLNO)
     
    // Read  
    if (action.read(EXTDTV)) {       
      mi.outData.put("CONO", EXTDTV.get("EXCONO").toString())
      mi.outData.put("DIVI", EXTDTV.getString("EXDIVI"))
      mi.outData.put("DLNO", EXTDTV.get("EXDLNO").toString())
      mi.outData.put("LOAD", EXTDTV.get("EXLOAD").toString())
      mi.outData.put("VLOG", EXTDTV.get("EXVLOG").toString())
      mi.outData.put("GVBF", EXTDTV.getDouble("EXGVBF").toString())
      mi.outData.put("NVBF", EXTDTV.getDouble("EXNVBF").toString())
      mi.write()
    } else {
      mi.error("No record found")   
      return 
    }
  } 
  
}