// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-09-10
// @version   1.0 
//
// Description 
// This API is to get a contract brand from EXTCTT
// Transaction GetContractTrip
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: CTNO - Contract Number
 * @param: RVID - Revision ID
 * @param: DLFY - Deliver From Yard
 * @param: DLTY - Deliver To Yard
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: DIVI - Division
 * @return: CTNO - Contract Number
 * @return: RVID - Revision ID
 * @return: DLFY - Deliver From Yard
 * @return: DLTY - Deliver To Yard
 * @return: TRRA - Trip Rate
 * @return: MTRA - Minimum Amount
 * 
*/



public class GetContractTrip extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database 
  private final ProgramAPI program
  private final LoggerAPI logger
  
  Integer inCONO
  String inDIVI
  int inCTNO
  String inRVID
  String inDLFY
  String inDLTY
  
  // Constructor 
  public GetContractTrip(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger) {
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

     // Contract Number
     if (mi.in.get("CTNO") != null) {
        inCTNO = mi.in.get("CTNO") 
     } else {
        inCTNO = 0      
     }
     
     // Revision ID
     if (mi.in.get("RVID") != null) {
        inRVID = mi.inData.get("RVID").trim() 
     } else {
        inRVID = ""      
     }

     // Deliver From Yard
     if (mi.in.get("DLFY") != null) {
        inDLFY = mi.inData.get("DLFY").trim() 
     } else {
        inDLFY = ""      
     }

     // Deliver To Yard
     if (mi.in.get("DLTY") != null) {
        inDLTY = mi.inData.get("DLTY").trim() 
     } else {
        inDLTY = ""      
     }
     
     // Get record
     getRecord()
  }
 
 //******************************************************************** 
 //Get EXTCTT record
 //********************************************************************     
  void getRecord(){      
     DBAction action = database.table("EXTCTT").index("00").selectAllFields().build()
     DBContainer EXTCTT = action.getContainer()
      
     // Key value for read
     EXTCTT.set("EXCONO", inCONO)
     EXTCTT.set("EXDIVI", inDIVI)
     EXTCTT.set("EXCTNO", inCTNO)
     EXTCTT.set("EXRVID", inRVID)
     EXTCTT.set("EXDLFY", inDLFY)
     EXTCTT.set("EXDLTY", inDLTY)
     
    // Read  
    if (action.read(EXTCTT)) {       
      mi.outData.put("CONO", EXTCTT.get("EXCONO").toString())
      mi.outData.put("DIVI", EXTCTT.getString("EXDIVI"))
      mi.outData.put("CTNO", EXTCTT.get("EXCTNO").toString())
      mi.outData.put("RVID", EXTCTT.getString("EXRVID"))
      mi.outData.put("DLFY", EXTCTT.getString("EXDLFY"))
      mi.outData.put("DLTY", EXTCTT.getString("EXDLTY"))
      mi.outData.put("TRRA", EXTCTT.getDouble("EXTRRA").toString())
      mi.outData.put("MTRA", EXTCTT.getDouble("EXMTRA").toString())
      mi.write()
    } else {
      mi.error("No record found")   
      return 
    }
  } 
  
}