// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to get payee split from EXTDPS
// Transaction GetPayeeSplit
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DLNO - Delivery Number
 * @param: STID - Scale Ticket ID
 * @param: ITNO - Item Number
 * @param: SEQN - Sequence
 * 
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: DIVI - Division
 * @return: DLNO - Delivery Number
 * @return: STID - Scale Ticket ID
 * @return: ITNO - Item Number
 * @return: SEQN - Sequence
 * @return: CASN - Payee Number
 * @return: SUNM - Payee Name
 * @return: CF15 - Payee Role
 * @return: SUCM - Cost Element
 * @return: INBN - Invoice Batch number
 * @return: CAAM - Charge Amount
 * 
*/



public class GetPayeeSplit extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database 
  private final ProgramAPI program
  
  Integer inCONO
  String inDIVI
  int inDLNO
  int inSTID
  String inITNO
  int inSEQN
  
  // Constructor 
  public GetPayeeSplit(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
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

     // Scale Ticket ID
     if (mi.in.get("STID") != null) {
        inSTID = mi.in.get("STID") 
     } else {
        inSTID = 0         
     }

     // Item Number
     if (mi.in.get("ITNO") != null) {
        inITNO = mi.in.get("ITNO") 
     } else {
        inITNO = ""         
     }

     // Sequence
     if (mi.in.get("SEQN") != null) {
        inSEQN = mi.in.get("SEQN") 
     } else {
        inSEQN = 0         
     }

     // Get record
     getRecord()
  }
 
 //******************************************************************** 
 //Get EXTDPS record
 //********************************************************************     
  void getRecord(){      
     DBAction action = database.table("EXTDPS").index("00").selectAllFields().build()
     DBContainer EXTDPS = action.getContainer()
     EXTDPS.set("EXCONO", inCONO)
     EXTDPS.set("EXDIVI", inDIVI)
     EXTDPS.set("EXDLNO", inDLNO)
     EXTDPS.set("EXSTID", inSTID)
     EXTDPS.set("EXITNO", inITNO)
     EXTDPS.set("EXSEQN", inSEQN)
     
     // Read  
     if (action.read(EXTDPS)) {       
        mi.outData.put("CONO", EXTDPS.get("EXCONO").toString())
        mi.outData.put("DIVI", EXTDPS.getString("EXDIVI"))
        mi.outData.put("DLNO", EXTDPS.get("EXDLNO").toString())
        mi.outData.put("STID", EXTDPS.get("EXSTID").toString())
        mi.outData.put("ITNO", EXTDPS.getString("EXITNO"))
        mi.outData.put("SEQN", EXTDPS.get("EXSEQN").toString())
        mi.outData.put("CASN", EXTDPS.getString("EXCASN"))
        mi.outData.put("SUNM", EXTDPS.getString("EXSUNM"))
        mi.outData.put("CF15", EXTDPS.get("EXCF15").toString())
        mi.outData.put("SUCM", EXTDPS.getString("EXSUCM"))
        mi.outData.put("INBN", EXTDPS.get("EXINBN").toString())
        mi.outData.put("CAAM", EXTDPS.getDouble("EXCAAM").toString())
        mi.write() 
      } else {
        mi.error("No record found")   
        return 
      }
  }
  
}