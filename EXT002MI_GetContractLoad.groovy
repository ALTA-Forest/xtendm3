// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to get a contract load from EXTCTL
// Transaction GetContractLoad
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DLNO - Delivery Number
 * @param: CTNO - Contract Number
 * @param: RVID - Revision ID
 * 
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: DIVI - Division
 * @return: DLNO - Delivery Number
 * @return: CTNO - Contract Number
 * @return: RVID - Revision ID
 * @return: TNLB - Total Net lbs
 * @return: TNBF - Total Net bf
 * @return: AVBL - Average bf/lbs
 * @return: AMNT - Amount
 * 
*/



public class GetContractLoad extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database 
  private final ProgramAPI program
  
  Integer inCONO
  String inDIVI
  int inDLNO
  int inCTNO
  String inRVID
  
  // Constructor 
  public GetContractLoad(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
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

     // Delivery Number
     if (mi.in.get("DLNO") != null) {
        inDLNO = mi.in.get("DLNO") 
     } else {
        inDLNO = 0         
     }

     // Contract Number
     if (mi.in.get("CTNO") != null) {
        inCTNO = mi.in.get("CTNO") 
     } else {
        inCTNO = 0         
     }

     // Revision ID
     if (mi.in.get("RVID") != null) {
        inRVID = mi.in.get("RVID") 
     } else {
        inRVID = ""         
     }

     // Get record
     getRecord()
  }
 
 //******************************************************************** 
 //Get EXTCTL record
 //********************************************************************     
  void getRecord(){      
     DBAction action = database.table("EXTCTL").index("00").selectAllFields().build()
     DBContainer EXTCTL = action.getContainer()
     EXTCTL.set("EXCONO", inCONO)
     EXTCTL.set("EXDIVI", inDIVI)
     EXTCTL.set("EXDLNO", inDLNO)
     EXTCTL.set("EXCTNO", inCTNO)
     EXTCTL.set("EXRVID", inRVID)
     
    // Read  
    if (action.read(EXTCTL)) {       
      mi.outData.put("CONO", EXTCTL.get("EXCONO").toString())
      mi.outData.put("DIVI", EXTCTL.getString("EXDIVI"))
      mi.outData.put("DLNO", EXTCTL.get("EXDLNO").toString())
      mi.outData.put("CTNO", EXTCTL.get("EXCTNO").toString())
      mi.outData.put("RVID", EXTCTL.getString("EXRVID"))
      mi.outData.put("TNLB", EXTCTL.getDouble("EXTNLB").toString())
      mi.outData.put("TNBF", EXTCTL.getDouble("EXTNBF").toString())
      mi.outData.put("AVBL", EXTCTL.getDouble("EXAVBL").toString())
      mi.outData.put("AMNT", EXTCTL.getDouble("EXAMNT").toString())
      mi.write() 
    } else {
      mi.error("No record found")   
      return 
    }
  }  
}