// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-05-10
// @version   1.0 
//
// Description 
// This API is to get a scale ticket record from EXTDST
// Transaction GetScaleTicket
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DLNO - Delivery Number
 * @param: STNO - Scale Ticket Number
 * 
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: DIVI - Division
 * @return: DLNO - Delivery Number
 * @return: STNO - Scale Ticket Number
 * @return: STDT - Scale Date
 * @return: STLR - Log Rule
 * @return: STLN - Scale Location Number
 * @return: STSN - Scaler Number
 * @return: STID - Scale Ticket ID
 * @return: STLP - Log Percentage
 * 
*/



public class GetScaleTicket extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database 
  private final ProgramAPI program
  private final LoggerAPI logger
  
  Integer inCONO
  String inDIVI
  int inDLNO
  String inSTNO
  
  // Constructor 
  public GetScaleTicket(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger) {
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

     // Scale Ticket Number
     if (mi.in.get("STNO") != null) {
        inSTNO = mi.in.get("STNO") 
     } else {
        inSTNO = ""         
     }

     // Get record
     getRecord()
  }
 
 //******************************************************************** 
 //Get EXTDST record
 //********************************************************************     
  void getRecord(){      
     DBAction action = database.table("EXTDST").index("00").selectAllFields().build()
     DBContainer EXTDST = action.getContainer()
     EXTDST.set("EXCONO", inCONO)
     EXTDST.set("EXDIVI", inDIVI)
     EXTDST.set("EXDLNO", inDLNO)
     EXTDST.set("EXSTNO", inSTNO.trim())
     
     // Send output value  
     if (action.read(EXTDST)) {  
        mi.outData.put("CONO", EXTDST.get("EXCONO").toString())
        mi.outData.put("DIVI", EXTDST.getString("EXDIVI"))
        mi.outData.put("DLNO", EXTDST.get("EXDLNO").toString())
        mi.outData.put("STLR", EXTDST.get("EXSTLR").toString())
        mi.outData.put("STNO", EXTDST.getString("EXSTNO"))
        mi.outData.put("STDT", EXTDST.get("EXSTDT").toString())
        mi.outData.put("STLR", EXTDST.get("EXSTLR").toString())
        mi.outData.put("STLN", EXTDST.getString("EXSTLN"))
        mi.outData.put("STSN", EXTDST.getString("EXSTSN"))
        mi.outData.put("STID", EXTDST.get("EXSTID").toString())
        mi.outData.put("STLP", EXTDST.get("EXSTLP").toString())        
        mi.write()   
     } else {
        mi.error("No record found")   
        return 
     }
  }
  
}