// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-05-10
// @version   1.0 
//
// Description 
// This API is to get a contract payee from EXTDLH
// Transaction GetDelivery
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
 * @return: DLTP - Delivery Type
 * @return: STAT - Status
 * @return: DLDT - Delivery Date
 * @return: SUNO - Supplier
 * @return: BUYE - Buyer
 * @return: DLTY - Deliver To Yard
 * @return: RCPN - Receipt Number
 * @return: TRPN - Trip Ticket Number
 * @return: TRCK - Truck
 * @return: BRND - Brand
 * @return: DLST - Delivery Sub Type
 * @return: CTNO - Contract Number
 * @return: ALHA - Alternate Hauler
 * @return: ISZP - Zero Payment
 * @return: RVID - Revision Id
 * @return: ISPS - Payee Split
 * @return: VLDT - Validate Date
 * @return: NOTE - Notes
*/


public class GetDelivery extends ExtendM3Transaction {
  private final MIAPI mi; 
  private final DatabaseAPI database; 
  private final ProgramAPI program;
  
  Integer CONO
  String DIVI
  int inDLNO
  
  // Constructor 
  public GetDelivery(MIAPI mi, DatabaseAPI database,ProgramAPI program) {
     this.mi = mi;
     this.database = database;  
     this.program = program;
  } 
    
  public void main() { 
     // Set Company Number
     CONO = mi.in.get("CONO")      
     if (CONO == null || CONO == 0) {
        CONO = program.LDAZD.CONO as Integer
     } 

     // Set Division
     DIVI = mi.in.get("DIVI")
     if (DIVI == null || DIVI == "") {
        DIVI = program.LDAZD.DIVI
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
 //Get EXTDLH record
 //********************************************************************     
  void getRecord(){      
     DBAction action = database.table("EXTDLH").index("00").selectAllFields().build()
     DBContainer EXTDLH = action.getContainer()
      
     // Key value for read
     EXTDLH.set("EXCONO", CONO)
     EXTDLH.set("EXDIVI", DIVI)
     EXTDLH.set("EXDLNO", inDLNO)
     
    // Read  
    if (action.read(EXTDLH)) {  
      mi.outData.put("CONO", EXTDLH.get("EXCONO").toString())
      mi.outData.put("DIVI", EXTDLH.getString("EXDIVI"))
      mi.outData.put("DLNO", EXTDLH.get("EXDLNO").toString())
      mi.outData.put("DLTP", EXTDLH.get("EXDLTP").toString())
      mi.outData.put("STAT", EXTDLH.get("EXSTAT").toString())
      mi.outData.put("DLDT", EXTDLH.get("EXDLDT").toString())
      mi.outData.put("SUNO", EXTDLH.getString("EXSUNO"))
      mi.outData.put("BUYE", EXTDLH.getString("EXBUYE"))
      mi.outData.put("DLFY", EXTDLH.getString("EXDLFY"))
      mi.outData.put("DLTY", EXTDLH.getString("EXDLTY"))
      mi.outData.put("FDCK", EXTDLH.get("EXFDCK").toString())
      mi.outData.put("TDCK", EXTDLH.get("EXTDCK").toString())
      mi.outData.put("RCPN", EXTDLH.getString("EXRCPN"))
      mi.outData.put("TRPN", EXTDLH.getString("EXTRPN"))
      mi.outData.put("TRCK", EXTDLH.getString("EXTRCK"))
      mi.outData.put("BRND", EXTDLH.getString("EXBRND"))
      mi.outData.put("DLST", EXTDLH.get("EXDLST").toString())
      mi.outData.put("CTNO", EXTDLH.get("EXCTNO").toString())
      mi.outData.put("ALHA", EXTDLH.get("EXALHA").toString())
      mi.outData.put("ISZP", EXTDLH.get("EXISZP").toString())
      mi.outData.put("FACI", EXTDLH.getString("EXFACI"))
      mi.outData.put("RVID", EXTDLH.getString("EXRVID"))
      mi.outData.put("ISPS", EXTDLH.get("EXISPS").toString())
      mi.outData.put("VLDT", EXTDLH.getString("EXVLDT"))
      mi.outData.put("NOTE", EXTDLH.getString("EXNOTE"))
      mi.write()  
    } else {
      mi.error("No record found")   
      return 
    }
  }  
}