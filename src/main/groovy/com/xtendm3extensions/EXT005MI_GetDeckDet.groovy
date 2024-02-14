// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to get a deck details from EXTDPD
// Transaction GetDeckDet
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
*/



public class GetDeckDet extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database 
  private final ProgramAPI program
  
  Integer inCONO
  String inDIVI
  int inDPID
  
  // Constructor 
  public GetDeckDet(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
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

     // Deck ID
     if (mi.in.get("DPID") != null) {
        inDPID = mi.in.get("DPID") 
     } else {
        inDPID = 0        
     }
 
    
     // Get record
     getRecord()
  }
 
 //******************************************************************** 
 //Get EXTDPD record
 //********************************************************************     
  void getRecord(){      
     DBAction action = database.table("EXTDPD").index("00").selectAllFields().build()
     DBContainer EXTDPD = action.getContainer()
     EXTDPD.set("EXCONO", inCONO)
     EXTDPD.set("EXDIVI", inDIVI)
     EXTDPD.set("EXDPID", inDPID)
     
    // Read  
    if (action.read(EXTDPD)) {       
      mi.outData.put("CONO", EXTDPD.get("EXCONO").toString())
      mi.outData.put("DIVI", EXTDPD.getString("EXDIVI"))
      mi.outData.put("DPID", EXTDPD.get("EXDPID").toString()) 
      mi.outData.put("LOAD", EXTDPD.getDouble("EXLOAD").toString()) 
      mi.outData.put("DLOG", EXTDPD.getDouble("EXDLOG").toString()) 
      mi.outData.put("GVBF", EXTDPD.getDouble("EXGVBF").toString()) 
      mi.outData.put("NVBF", EXTDPD.getDouble("EXNVBF").toString()) 
      mi.outData.put("ESWT", EXTDPD.getDouble("EXESWT").toString()) 
      mi.outData.put("TCOI", EXTDPD.getDouble("EXCOST").toString()) 
      mi.outData.put("AWBF", EXTDPD.getDouble("EXAWBF").toString()) 
      mi.outData.put("GBFL", EXTDPD.getDouble("EXGBFL").toString()) 
      mi.outData.put("NBFL", EXTDPD.getDouble("EXNBFL").toString()) 
      mi.write()       
    } else {
      mi.error("No record found")   
      return             
    }
  } 
  
}