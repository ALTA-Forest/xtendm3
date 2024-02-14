// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to get an account from EXTACT
// Transaction GetAccount
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: ACCD - Account
 * 
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: DIVI - Division
 * @return: ACCD - Account
 * @return: NAME - Name
 * 
*/


public class GetAccount extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database 
  private final ProgramAPI program
  
  Integer inCONO
  String inDIVI
  String inACCD
  
  // Constructor 
  public GetAccount(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
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

     // Account Code
     if (mi.in.get("ACCD") != null) {
        inACCD = mi.in.get("ACCD") 
     } else {
        inACCD = ""         
     }
     
     // Get record
     getRecord()
  }
 
 //******************************************************************** 
 //Get EXTACT record
 //********************************************************************     
  void getRecord(){      
     DBAction action = database.table("EXTACT").index("00").selectAllFields().build()
     DBContainer EXTACT = action.getContainer()
     EXTACT.set("EXCONO", inCONO)
     EXTACT.set("EXDIVI", inDIVI)
     EXTACT.set("EXACCD", inACCD)
     
    // Read  
    if (action.read(EXTACT)) {       
      mi.outData.put("CONO", EXTACT.get("EXCONO").toString())
      mi.outData.put("DIVI", EXTACT.getString("EXDIVI"))
      mi.outData.put("ACCD", EXTACT.getString("EXACCD")) 
      mi.outData.put("NAME", EXTACT.getString("EXNAME")) 
      mi.write() 
    } else {
      mi.error("No record found")   
      return 
    }
  } 
  
}