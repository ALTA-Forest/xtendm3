// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-08-10
// @version   1.0 
//
// Description 
// This API is to get a scale ticket record from EXTDSP
// Transaction GetScaleTktPur
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DLNO - Delivery Number
 * @param: CTNO - Contract Number
 * @param: SUNO - Supplier
 * 
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: DIVI - Division
 * @return: STID - Scale Ticket ID
 * @return: DLNO - Delivery Number
 * @return: CTNO - Contract Number
 * @return: TREF - Reference
 * @return: FACI - Facility
 * @return: DLDT - Delivery Date
 * @return: SUNO - Supplier
 * @return: YARD - Yard
 * @return: MSGN - Message Number
 * @return: INBN - Invoice Batch Number
 * 
*/


public class GetScaleTktPur extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database 
  private final ProgramAPI program
  private final LoggerAPI logger
  
  Integer inCONO
  String inDIVI
  int inDLNO
  int inSTID
  
  // Constructor 
  public GetScaleTktPur(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger) {
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

     // Scale Ticket ID
     if (mi.in.get("STID") != null) {
        inSTID = mi.in.get("STID") 
     } else {
        inSTID = 0        
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
 //Get EXTDSP record
 //********************************************************************     
  void getRecord(){      
     DBAction action = database.table("EXTDSP").index("00").selectAllFields().build()
     DBContainer EXTDSP = action.getContainer()
     EXTDSP.set("EXCONO", inCONO)
     EXTDSP.set("EXDIVI", inDIVI)
     EXTDSP.set("EXSTID", inSTID)
     EXTDSP.set("EXDLNO", inDLNO)
     
     
     if (action.read(EXTDSP)) {  
        mi.outData.put("CONO", EXTDSP.get("EXCONO").toString())
        mi.outData.put("DIVI", EXTDSP.getString("EXDIVI"))
        mi.outData.put("STID", EXTDSP.get("EXSTID").toString())
        mi.outData.put("DLNO", EXTDSP.get("EXDLNO").toString())
        mi.outData.put("CTNO", EXTDSP.get("EXCTNO").toString())
        mi.outData.put("TREF", EXTDSP.getString("EXTREF"))
        mi.outData.put("FACI", EXTDSP.get("EXFACI").toString())
        mi.outData.put("DLDT", EXTDSP.get("EXDLDT").toString())
        mi.outData.put("SUNO", EXTDSP.getString("EXSUNO"))
        mi.outData.put("YARD", EXTDSP.getString("EXYARD"))
        mi.outData.put("MSGN", EXTDSP.getString("EXMSGN"))
        mi.outData.put("INBN", EXTDSP.get("EXINBN").toString())
        mi.outData.put("SPEC", EXTDSP.get("EXSPEC").toString())
        mi.outData.put("LOGS", EXTDSP.getString("EXLOGS"))
        mi.outData.put("AMNT", EXTDSP.getString("EXAMNT"))
        mi.write()   
      } else {
        mi.error("No record found")   
        return 
      }
    }
  
}