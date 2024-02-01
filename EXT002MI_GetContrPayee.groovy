// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to get a contract payee from EXTCTP
// Transaction GetContrPayee
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: RVID - Revision ID
 * @param: CPID - Payee ID
 * @param: CASN - Payee Number
 * 
*/

/**
 * OUT
 * @return: RVID - Revision ID
 * @return: CPID - Payee ID
 * @return: CASN - Payee Number
 * @return: PYNM - Payee Name
 * @return: CF15 - Payee Role
 * @return: SHTP - Share Type
 * @return: CATF - Take From ID
 * @return: TFNM - Take From Name
 * @return: CATP - Take Priority
 * @return: CAAM - Amount
 * @return: CASA - Test Share
 * @return: PLVL - Level
 * @return: SLVL - Sub-Level
 * @return: PPID - Parent Payee ID
 * 
*/



public class GetContrPayee extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database 
  private final ProgramAPI program
  
  Integer inCONO
  String inDIVI
  String inRVID
  int inCPID
  
  // Constructor 
  public GetContrPayee(MIAPI mi, DatabaseAPI database,ProgramAPI program) {
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

     // Revision ID
     if (mi.in.get("RVID") != null) {
        inRVID = mi.in.get("RVID") 
     } else {
        inRVID = ""         
     }

     // Payee ID
     if (mi.in.get("CPID") != null) {
        inCPID = mi.in.get("CPID") 
     } else {
        inCPID = 0        
     }
     
     // Get record
     getRecord()
  }
 
 //******************************************************************** 
 //Get EXTCTP record
 //********************************************************************     
  void getRecord(){      
     DBAction action = database.table("EXTCTP").index("00").selectAllFields().build()
     DBContainer EXTCTP = action.getContainer()
      
     // Key value for read
     EXTCTP.set("EXCONO", inCONO)
     EXTCTP.set("EXDIVI", inDIVI)
     EXTCTP.set("EXRVID", inRVID)
     EXTCTP.set("EXCPID", inCPID)
     
    // Read  
    if (action.read(EXTCTP)) {       
      // Send output value 
      mi.outData.put("CONO", EXTCTP.get("EXCONO").toString())
      mi.outData.put("DIVI", EXTCTP.getString("EXDIVI"))
      mi.outData.put("RVID", EXTCTP.getString("EXRVID"))
      mi.outData.put("CPID", EXTCTP.get("EXCPID").toString())
      mi.outData.put("CASN", EXTCTP.getString("EXCASN"))
      mi.outData.put("PYNM", EXTCTP.getString("EXSUNM"))
      mi.outData.put("CF15", EXTCTP.get("EXCF15").toString())
      mi.outData.put("SHTP", EXTCTP.getString("EXSHTP"))
      mi.outData.put("CATF", EXTCTP.getString("EXCATF"))
      mi.outData.put("TFNM", EXTCTP.getString("EXTFNM"))
      mi.outData.put("CATP", EXTCTP.get("EXCATP").toString())
      mi.outData.put("CAAM", EXTCTP.getDouble("EXCAAM").toString())
      mi.outData.put("CASA", EXTCTP.getDouble("EXCASA").toString())
      mi.outData.put("PLVL", EXTCTP.get("EXPLVL").toString())
      mi.outData.put("SLVL", EXTCTP.get("EXSLVL").toString())
      mi.outData.put("PPID", EXTCTP.get("EXPPID").toString())
      mi.outData.put("ISAH", EXTCTP.get("EXISAH").toString())
      mi.outData.put("TRCK", EXTCTP.getString("EXTRCK"))
      mi.write()   
    } else {
      mi.error("No record found")   
      return 
    }
  }  
}