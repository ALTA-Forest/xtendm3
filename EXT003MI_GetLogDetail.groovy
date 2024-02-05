// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-05-10
// @version   1.0 
//
// Description 
// This API is to get a log detail record from EXTSLD
// Transaction GetLogDetail
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: STID - Scale Ticket ID
 * @param: LDID - Log Detaild ID
 * 
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: DIVI - Division
 * @return: STID - Scale Ticket ID
 * @return: LDID - Log Detail ID
 * @return: LGID - Log ID
 * @return: GRAD - Grade
 * @return: LLEN - Length
 * @return: LEND - Length Deduction
 * @return: LSDI - Small Diameter
 * @return: LLDI - Large Diameter
 * @return: DIAD - Diameter Deduction
 * @return: LGRV - Gross Volume
 * @return: LNEV - Net Volume
 * 
*/


public class GetLogDetail extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database 
  private final ProgramAPI program
  private final LoggerAPI logger
  
  Integer inCONO
  String inDIVI
  int inLDID
  int inSTID
  
  // Constructor 
  public GetLogDetail(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger) {
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

     // Log Detail ID
     if (mi.in.get("LDID") != null) {
        inLDID = mi.in.get("LDID") 
     } else {
        inLDID = 0         
     }

     // Get record
     getRecord()
  }
 
 //******************************************************************** 
 //Get EXTSLD record
 //********************************************************************     
  void getRecord(){      
     DBAction action = database.table("EXTSLD").index("00").selectAllFields().build()
     DBContainer EXTSLD = action.getContainer()
     EXTSLD.set("EXCONO", inCONO)
     EXTSLD.set("EXDIVI", inDIVI)
     EXTSLD.set("EXSTID", inSTID)
     EXTSLD.set("EXLDID", inLDID)
     
     // Send output value  
     if (action.read(EXTSLD)) {  
        mi.outData.put("CONO", EXTSLD.get("EXCONO").toString())
        mi.outData.put("DIVI", EXTSLD.getString("EXDIVI"))
        mi.outData.put("STID", EXTSLD.get("EXSTID").toString())
        mi.outData.put("LDID", EXTSLD.get("EXLDID").toString())
        mi.outData.put("LGID", EXTSLD.get("EXLGID").toString())
        mi.outData.put("GRAD", EXTSLD.getString("EXGRAD"))
        mi.outData.put("LLEN", EXTSLD.getDouble("EXLLEN").toString())
        mi.outData.put("LEND", EXTSLD.getDouble("EXLEND").toString())
        mi.outData.put("LSDI", EXTSLD.getDouble("EXLSDI").toString())
        mi.outData.put("LLDI", EXTSLD.getDouble("EXLLDI").toString())
        mi.outData.put("DIAD", EXTSLD.getDouble("EXDIAD").toString())
        mi.outData.put("LGRV", EXTSLD.getDouble("EXLGRV").toString())
        mi.outData.put("LNEV", EXTSLD.getDouble("EXLNEV").toString())       
        mi.write()
     } else {
        mi.error("No record found")   
        return 
     }
  }
  
}