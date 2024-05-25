// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-05-10
// @version   1.0 
//
// Description 
// This API is to get a contract payee from EXTDSL
// Transaction GetScaleTktLn
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: STID - Scale Ticket ID
 * @param: PONR - Line Number
 * @param: ITNO - Item Number
 * 
*/

/**
 * OUT
 * @return: STID - Scale Ticket ID
 * @return: PONR - Line Number
 * @return: ITNO - Item Number
 * @return: ORQT - Quantity
 * @return: STAM - Share Amount
 * 
*/



public class GetScaleTktLn extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database 
  private final ProgramAPI program
  
  Integer inCONO
  String inDIVI
  int inSTID
  int inPONR
  String inITNO
  
  // Constructor 
  public GetScaleTktLn(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
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

     // Scale Ticket ID
     if (mi.in.get("STID") != null) {
        inSTID = mi.in.get("STID") 
     } else {
        inSTID = 0         
     }

     // Line Number
     if (mi.in.get("PONR") != null) {
        inPONR = mi.in.get("PONR") 
     } else {
        inPONR = 0         
     }

     // Item Number
     if (mi.in.get("ITNO") != null && mi.in.get("ITNO") != "") {
        inITNO = mi.inData.get("ITNO").trim() 
     } else {
        inITNO = ""         
     }

     // Get record
     getRecord()
  }
 
 //******************************************************************** 
 //Get EXTDSL record
 //********************************************************************     
  void getRecord(){      
     DBAction action = database.table("EXTDSL").index("00").selectAllFields().build()
     DBContainer EXTDSL = action.getContainer()
     EXTDSL.set("EXCONO", inCONO)
     EXTDSL.set("EXDIVI", inDIVI)
     EXTDSL.set("EXSTID", inSTID)
     EXTDSL.set("EXPONR", inPONR)
     EXTDSL.set("EXITNO", inITNO)    
     
     // Read  
     if (action.read(EXTDSL)) {  
        mi.outData.put("CONO", EXTDSL.get("EXCONO").toString())
        mi.outData.put("DIVI", EXTDSL.getString("EXDIVI"))
        mi.outData.put("STID", EXTDSL.get("EXSTID").toString())
        mi.outData.put("PONR", EXTDSL.get("EXPONR").toString())
        mi.outData.put("ITNO", EXTDSL.getString("EXITNO"))
        mi.outData.put("ORQT", EXTDSL.get("EXORQT").toString())
        mi.outData.put("STAM", EXTDSL.get("EXSTAM").toString())
        mi.write() 
     } else {
        mi.error("No record found")   
        return 
     }
  }  
  
}