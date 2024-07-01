// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to get a deck register from EXTDPR
// Transaction GetDeckRegister
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DPID - Deck ID
 * @param: TRNO - Transaction Number
 * 
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: DIVI - Division
 * @return: DPID - Deck ID
 * @return: TRNO - Transaction Number
 * @return: SPEC - Species
 * @return: PUNO - Purchase Order Number
 * @return: INBN - Invoice Batch Number
 * @return: TRDT - Transaction Date
 * @return: TRTP - Transaction Type
 * @return: ACCD - Account Code
 * @return: ACNM - Account Name
 * @return: TRRE - Transaction Receipt
 * @return: TRTT - Transaction Ticket
 * @return: LOAD - Load
 * @return: TLOG - Logs
 * @return: TGBF - Gross BF
 * @return: TNBF - Net BF
 * @return: AUWE - Automatic Volume
 * @return: GRWE - Gross Weight
 * @return: TRWE - Tare Weight
 * @return: NEWE - Net Weight
 * @return: DAAM - Amount
 * @return: RPID - Reason ID
 * 
*/


public class GetDeckRegister extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database 
  private final ProgramAPI program
  
  Integer inCONO
  String inDIVI
  int inDPID
  int inTRNO
  
  // Constructor 
  public GetDeckRegister(MIAPI mi, DatabaseAPI database,ProgramAPI program) {
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
 
      // Transaction Number
     if (mi.in.get("TRNO") != null) {
        inTRNO = mi.in.get("TRNO") 
     } else {
        inTRNO = 0        
     }

    
     // Get record
     getRecord()
  }
 
 //******************************************************************** 
 //Get EXTDPD record
 //********************************************************************     
  void getRecord(){      
     DBAction action = database.table("EXTDPR").index("00").selectAllFields().build()
     DBContainer EXTDPR = action.getContainer()
     EXTDPR.set("EXCONO", inCONO)
     EXTDPR.set("EXDIVI", inDIVI)
     EXTDPR.set("EXDPID", inDPID)
     EXTDPR.set("EXTRNO", inTRNO)
     
    // Read  
    if (action.read(EXTDPR)) {       
      mi.outData.put("CONO", EXTDPR.get("EXCONO").toString())
      mi.outData.put("DIVI", EXTDPR.getString("EXDIVI"))
      mi.outData.put("DPID", EXTDPR.get("EXDPID").toString()) 
      mi.outData.put("TRNO", EXTDPR.get("EXTRNO").toString()) 
      mi.outData.put("SPEC", EXTDPR.getString("EXSPEC")) 
      mi.outData.put("TREF", EXTDPR.getString("EXTREF"))
      mi.outData.put("INBN", EXTDPR.get("EXINBN").toString()) 
      mi.outData.put("TRDT", EXTDPR.get("EXTRDT").toString()) 
      mi.outData.put("TRTP", EXTDPR.get("EXTRTP").toString()) 
      mi.outData.put("ACCD", EXTDPR.getString("EXACCD")) 
      mi.outData.put("ACNM", EXTDPR.getString("EXACNM")) 
      mi.outData.put("TRRE", EXTDPR.getString("EXTRRE")) 
      mi.outData.put("TRTT", EXTDPR.getString("EXTRTT")) 
      mi.outData.put("LOAD", EXTDPR.get("EXLOAD").toString()) 
      mi.outData.put("TLOG", EXTDPR.get("EXTLOG").toString()) 
      mi.outData.put("TGBF", EXTDPR.getDouble("EXTGBF").toString()) 
      mi.outData.put("TNBF", EXTDPR.getDouble("EXTNBF").toString()) 
      mi.outData.put("TRNB", EXTDPR.getDouble("EXTRNB").toString()) 
      mi.outData.put("AUWE", EXTDPR.get("EXAUWE").toString()) 
      mi.outData.put("GRWE", EXTDPR.getDouble("EXGRWE").toString()) 
      mi.outData.put("TRWE", EXTDPR.getDouble("EXTRWE").toString()) 
      mi.outData.put("NEWE", EXTDPR.getDouble("EXNEWE").toString()) 
      mi.outData.put("DAAM", EXTDPR.getDouble("EXDAAM").toString()) 
      mi.outData.put("RPID", EXTDPR.getString("EXRPID")) 
      mi.outData.put("NOTE", EXTDPR.getString("EXNOTE")) 
      mi.write() 
    } else {
      mi.error("No record found")   
      return 
    }
  }
  
}