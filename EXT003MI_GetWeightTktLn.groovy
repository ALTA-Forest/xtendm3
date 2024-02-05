// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-05-10
// @version   1.0 
//
// Description 
// This API is to get a weight ticket line from EXTDWT
// Transaction GetWeightTktLn
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



public class GetWeightTktLn extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database 
  private final ProgramAPI program
  
  Integer inCONO
  String inDIVI
  int inWTNO
  int inDLNO
  
  // Constructor 
  public GetWeightTktLn(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
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

     // Weight Ticket ID
     if (mi.in.get("WTNO") != null) {
        inWTNO = mi.in.get("WTNO") 
     } else {
        inWTNO = 0         
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
 //Get EXTDWT record
 //********************************************************************     
  void getRecord(){      
     DBAction action = database.table("EXTDWT").index("00").selectAllFields().build()
     DBContainer EXTDWT = action.getContainer()
     EXTDWT.set("EXCONO", inCONO)
     EXTDWT.set("EXDIVI", inDIVI)
     EXTDWT.set("EXWTNO", inWTNO)
     EXTDWT.set("EXDLNO", inDLNO)

    // Read  
    if (action.read(EXTDWT)) {       
      mi.outData.put("CONO", EXTDWT.get("EXCONO").toString())
      mi.outData.put("DIVI", EXTDWT.getString("EXDIVI"))
      mi.outData.put("WTNO", EXTDWT.get("EXWTNO").toString())
      mi.outData.put("WTKN", EXTDWT.getString("EXWTKN"))
      mi.outData.put("WTDT", EXTDWT.get("EXWTDT").toString())
      mi.outData.put("WTLN", EXTDWT.getString("EXWTLN"))
      mi.outData.put("DLNO", EXTDWT.get("EXDLNO").toString())
      mi.outData.put("GRWE", EXTDWT.get("EXGRWE").toString())
      mi.outData.put("TRWE", EXTDWT.get("EXTRWE").toString())
      mi.outData.put("NEWE", EXTDWT.get("EXNEWE").toString())      
      mi.write() 
    } else {
      mi.error("No record found")   
      return 
    }
    
  }
  
}