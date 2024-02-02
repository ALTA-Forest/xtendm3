// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-11-05
// @version   1.0 
//
// Description 
// This API is to update deliveries in EXTDLH and EXTSLH
// Transaction UpdDeliveries
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DNOS - Delivery Numbers
 * @param: STAT - Status
 * @param: DLTY - Deliver To Yard
 * @param: TDCK - To Deck
 * @param: SPEC - Species
 * @param: BRND - Brand
 * @param: ISZP - Zero Payment
 * @param: FACI - Facility
 * 
*/



public class UpdDeliveries extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final LoggerAPI logger
  private final UtilityAPI utility
  
  Integer inCONO
  String inDIVI
  String inDNOS
  String inSTAT
  String inDLTY
  int inTDCK
  String inSPEC
  String inBRND
  String inISZP
  String inFACI
  int scaleTicketID
  
  // Constructor 
  public UpdDeliveries(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, LoggerAPI logger, UtilityAPI utility) {
     this.mi = mi
     this.database = database
     this.miCaller = miCaller
     this.program = program
     this.logger = logger    
     this.utility = utility
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

     // Delivery Number string
     if (mi.in.get("DNOS") != null) {
        inDNOS = mi.inData.get("DNOS") 
     } else {
        inDNOS = ""         
     }
         
     // Status
     if (mi.in.get("STAT") != null) {
        inSTAT = mi.inData.get("STAT").trim() 
     } else {
        inSTAT = ""
     }
     
     // Deliver To Yard
     if (mi.in.get("DLTY") != null) {
        inDLTY = mi.inData.get("DLTY").trim() 
     } else {
        inDLTY = ""        
     }

     // To Deck
     if (mi.in.get("TDCK") != null) {
        inTDCK = mi.in.get("TDCK")
     } 

     // Species
     if (mi.in.get("SPEC") != null) {
        inSPEC = mi.inData.get("SPEC").trim() 
     } else {
        inSPEC = ""        
     }

     // Brand
     if (mi.in.get("BRND") != null) {
        inBRND = mi.inData.get("BRND").trim() 
     } else {
        inBRND = ""        
     }

     // Zero Payment
     if (mi.in.get("ISZP") != null) {
        inISZP = mi.inData.get("ISZP").trim() 
     } else {
        inISZP = "" 
     }
     
     // Facility
     if (mi.in.get("FACI") != null) {
        inFACI = mi.inData.get("FACI").trim() 
     } else {
        inFACI = ""        
     }
     
     String deliveryString = inDNOS
     String[] deliveries
     deliveries = deliveryString.split(',')
      
     for (String delivery : deliveries) {
        updDeliveryMI(String.valueOf(inCONO), inDIVI, delivery.trim(), inSTAT, inDLTY, inBRND, inISZP, inFACI) 
        
        scaleTicketID = 0
        
        // Get Scale Ticket info
        Optional<DBContainer> EXTDST = findEXTDST(inCONO, inDIVI, delivery.toInteger())
        if(EXTDST.isPresent()){
           DBContainer containerEXTDST = EXTDST.get() 
           scaleTicketID = containerEXTDST.get("EXSTID") 
           updEXTSLHRecord()
        }
     }
   
  }
  
  //******************************************************************** 
  // Get EXTDLH record
  //******************************************************************** 
  private Optional<DBContainer> findEXTDLH(int CONO, String DIVI, int DLNO){  
     DBAction query = database.table("EXTDLH").index("00").selection("EXCONO", "EXDIVI", "EXDLNO").build()
     DBContainer EXTDLH = query.getContainer()
     EXTDLH.set("EXCONO", CONO)
     EXTDLH.set("EXDIVI", DIVI)
     EXTDLH.set("EXDLNO", DLNO)
     if(query.read(EXTDLH))  { 
       return Optional.of(EXTDLH)
     } 
  
     return Optional.empty()
  }


  //******************************************************************** 
  // Get EXTDST record
  //******************************************************************** 
  private Optional<DBContainer> findEXTDST(int CONO, String DIVI, int DLNO){  
     DBAction query = database.table("EXTDST").index("00").selection("EXSTID").build()
     DBContainer EXTDST = query.getContainer()
     EXTDST.set("EXCONO", CONO)
     EXTDST.set("EXDIVI", DIVI)
     EXTDST.set("EXDLNO", DLNO)
     if(query.read(EXTDST))  { 
       return Optional.of(EXTDST)
     } 
  
     return Optional.empty()
  }


   //***************************************************************************** 
   // Update Delivery
   //***************************************************************************** 
   void updDeliveryMI(String company, String division, String delivery, String status, String deliverToYard, String brand, String zeroPayment, String facility){   
        Map<String, String> params = [CONO: company, DIVI: division, DLNO: delivery, STAT: status, DLTY: deliverToYard, BRND: brand, ISZP: zeroPayment, FACI: facility] 
        Closure<?> callback = {
          Map<String, String> response ->
        }

        miCaller.call("EXT003MI","UpdDelivery", params, callback)       
   } 


   //***************************************************************************** 
   // Update Log Header
   //***************************************************************************** 
   void updLogHeaderMI(String company, String division, String scaleTicketID, String sequenceNumber, String toDeck, String species){   
        Map<String, String> params = [CONO: company, DIVI: division, STID: scaleTicketID, SEQN: sequenceNumber, TDCK: toDeck, SPEC: species] 
        Closure<?> callback = {
          Map<String, String> response ->
        }

        miCaller.call("EXT003MI","UpdLogHeader", params, callback)        
   } 
   
   
  //******************************************************************** 
  // Update EXTSLH record
  //********************************************************************    
  void updEXTSLHRecord(){      
     DBAction action = database.table("EXTSLH").index("00").build()
     DBContainer EXTSLH = action.getContainer()     
     EXTSLH.set("EXCONO", inCONO)
     EXTSLH.set("EXDIVI", inDIVI)
     EXTSLH.set("EXSTID", scaleTicketID)

     // Read with lock
     action.readAllLock(EXTSLH, 3, updateCallBackEXTSLH)
     }
   
     Closure<?> updateCallBackEXTSLH = { LockedResult lockedResult -> 
       if (mi.in.get("TDCK") != null) {
          lockedResult.set("EXTDCK", mi.in.get("TDCK"))
       }
  
       if (inSPEC != "") {
          lockedResult.set("EXSPEC", inSPEC)
       }
     
       int changeNo = lockedResult.get("EXCHNO")
       int newChangeNo = changeNo + 1 
       int changedate = utility.call("DateUtil", "currentDateY8AsInt")
       lockedResult.set("EXLMDT", changedate)        
       lockedResult.set("EXCHNO", newChangeNo) 
       lockedResult.set("EXCHID", program.getUser())
       lockedResult.update()
    }

} 

