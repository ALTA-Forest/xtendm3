// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-08-10
// @version   1.0 
//
// Description 
// This API is to get a batch record from EXTDBH
// Transaction GetBatch
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DBNO - Batch Number
 * 
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: DIVI - Division
 * @return: DBNO - Batch Number
 * @return: DBTP - Batch Type
 * @return: DBBU - Business Unit
 * @return: BUNA - Business Unit Name
 * @return: BDEL - Deliveries
 * @return: BTOT - Total
 * @return: BDDT - Delivery Date Through
 * 
*/

public class GetBatch extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database 
  private final ProgramAPI program
  private final LoggerAPI logger
  
  Integer inCONO
  String inDIVI
  int inDBNO

  
  // Constructor 
  public GetBatch(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger) {
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
     inDIVI = mi.inData.get("DIVI").trim()
     if (inDIVI == null || inDIVI == "") {
        inDIVI = program.LDAZD.DIVI
     }

     // Batch Number
     if (mi.in.get("DBNO") != null) {
        inDBNO = mi.in.get("DBNO") 
     } else {
        inDBNO = 0        
     }


     // Get record
     getRecord()
  }
 
 //******************************************************************** 
 //Get EXTDBH record
 //********************************************************************     
  void getRecord(){      
     DBAction action = database.table("EXTDBH").index("00").selectAllFields().build()
     DBContainer EXTDBH = action.getContainer()
     EXTDBH.set("EXCONO", inCONO)
     EXTDBH.set("EXDIVI", inDIVI)
     EXTDBH.set("EXDBNO", inDBNO)  
     
     if (action.read(EXTDBH)) {  
        mi.outData.put("CONO", EXTDBH.get("EXCONO").toString())
        mi.outData.put("DIVI", EXTDBH.getString("EXDIVI"))
        mi.outData.put("DBNO", EXTDBH.get("EXDBNO").toString())
        mi.outData.put("DBTP", EXTDBH.get("EXDBTP").toString())
        mi.outData.put("DBBU", EXTDBH.getString("EXDBBU"))
        mi.outData.put("BUNA", EXTDBH.getString("EXBUNA"))
        mi.outData.put("BDEL", EXTDBH.get("EXBDEL").toString())
        mi.outData.put("BTOT", EXTDBH.get("EXBTOT").toString())
        mi.outData.put("STAT", EXTDBH.get("EXSTAT").toString())
        mi.outData.put("NOTE", EXTDBH.getString("EXNOTE"))
        mi.outData.put("BDDT", EXTDBH.get("EXBDDT").toString())
        mi.write() 
      } else {
        mi.error("No record found")   
        return 
      }
    }
  
}