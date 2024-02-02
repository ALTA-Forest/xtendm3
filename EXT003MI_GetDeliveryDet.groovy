// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-05-10
// @version   1.0 
//
// Description 
// This API is to get a contract payee from EXTDLD
// Transaction GetDeliveryDet
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DLNO - Delivery Number
 * 
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: DIVI - Division
 * @return: DLNO - Delivery Number
 * @return: WTNO - Weight Ticket Number
 * @return: STNO - Scale Ticket Number
 * 
*/



public class GetDeliveryDet extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database 
  private final ProgramAPI program
  
  Integer inCONO
  String inDIVI
  int inDLNO
  
  // Constructor 
  public GetDeliveryDet(MIAPI mi, DatabaseAPI database,ProgramAPI program) {
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

     // Get record
     getRecord()
  }
 
 //******************************************************************** 
 //Get EXTDLD record
 //********************************************************************     
  void getRecord(){      
     DBAction action = database.table("EXTDLD").index("00").selectAllFields().build()
     DBContainer EXTDLD = action.getContainer()
     EXTDLD.set("EXCONO", inCONO)
     EXTDLD.set("EXDIVI", inDIVI)
     EXTDLD.set("EXDLNO", inDLNO)     
     
    // Read  
    if (action.read(EXTDLD)) {  
      mi.outData.put("CONO", EXTDLD.get("EXCONO").toString())
      mi.outData.put("DIVI", EXTDLD.getString("EXDIVI"))
      mi.outData.put("DLNO", EXTDLD.get("EXDLNO").toString())
      mi.outData.put("CTNO", EXTDLD.get("EXCTNO").toString())
      mi.outData.put("WTNO", EXTDLD.get("EXWTNO").toString())
      mi.outData.put("STNO", EXTDLD.getString("EXSTNO"))
      mi.write() 
    } else {
      mi.error("No record found")   
      return 
    }
  }
  
}