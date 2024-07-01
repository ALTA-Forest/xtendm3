// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2024-05-16
// @version   1.0 
//
// Description 
// This API is will be used to validate the balance in EXTDPR/ESTDPD
// Transaction ValDeckBalance
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DPID - Deck ID
*/



public class ValDeckBalance extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final LoggerAPI logger
  private final UtilityAPI utility
  
  Integer inCONO
  String inDIVI
  int inDPID
  int inTRNO
  double netBF
  double netBalance
  int deckID
  int transactionNumber
  double sumNetBF
  double sumNetBalance
  double detailNVBF
  int resultDPR
  int resultDPD

  
  // Constructor 
  public ValDeckBalance(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, LoggerAPI logger, UtilityAPI utility) {
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

     // Deck ID
     if (mi.in.get("DPID") != null) {
        inDPID = mi.in.get("DPID") 
     } else {
        inDPID = 0         
     }


     // Validate Deck Head record
     Optional<DBContainer> EXTDPH = findEXTDPH(inCONO, inDIVI, inDPID)
     if(!EXTDPH.isPresent()){
        mi.error("Deck Head record doesn't exist")   
        return             
     }
     
     //List all transactions for the input deck
     netBF = 0d
     netBalance = 0d
     sumNetBF = 0d
     deckID = 0
     transactionNumber = 0
     List<DBContainer> resultEXTDPR = listEXTDPR(inCONO, inDIVI, inDPID) 
     for (DBContainer recLineEXTDPR : resultEXTDPR) { 
         //For each transaction, get the net balance and the net total balance and make sure it is calculated correctly
         //If the sum is matching, set the validate flag to 1, else set it to 0
          deckID = recLineEXTDPR.get("EXDPID")
          logger.debug("deckID ${deckID}")
          transactionNumber = recLineEXTDPR.get("EXTRNO")
          logger.debug("transactionNumber ${transactionNumber}")
          netBF = recLineEXTDPR.get("EXTNBF")
          sumNetBF = sumNetBF + netBF
          netBalance = recLineEXTDPR.get("EXTRNB")
          sumNetBalance = 0d
          sumNetBalance = netBalance
     }
     
     logger.debug("sumNetBF ${sumNetBF}")
     logger.debug("sumNetBalance ${sumNetBalance}")
     
     resultDPR = 0
     resultDPR = new Double(sumNetBF).compareTo(new Double(sumNetBalance))
     logger.debug("resultDPR ${resultDPR}")
     
     resultDPD = 0
     resultDPD = new Double(detailNVBF).compareTo(new Double(sumNetBalance))
     logger.debug("resultDPD ${resultDPD}")
     
     detailNVBF = 0d
     
     // Get Deck Profile Detail record
     Optional<DBContainer> EXTDPD = findEXTDPD(inCONO, inDIVI, inDPID)
     if(!EXTDPD.isPresent()){
        mi.error("Deck Profile Details doesn't exist")   
        return             
     } else {
        // Record found, get info from detail record  
        DBContainer containerEXTDPD = EXTDPD.get() 
        detailNVBF = containerEXTDPD.get("EXNVBF") 
     } 
    
     logger.debug("detailNVBF ${detailNVBF}")


     resultDPD = 0
     resultDPD = new Double(detailNVBF).compareTo(new Double(sumNetBalance))
     logger.debug("resultDPD ${resultDPD}")
     
     if (resultDPD == 0) {
        resultDPD = 1
     } else {
        resultDPD = 0
     }
     
     //Update EXTDPH with balanced flag
     updEXTDPHRecord()

     mi.outData.put("CONO", String.valueOf(inCONO)) 
     mi.outData.put("DIVI", inDIVI) 
     mi.outData.put("TRNO", String.valueOf(transactionNumber))      
     mi.outData.put("NVBF", String.valueOf(detailNVBF))      
     mi.outData.put("TRNB", String.valueOf(sumNetBalance))      
     mi.outData.put("ISDB", String.valueOf(resultDPD))      
     mi.write()
  }


   //******************************************************************** 
   // Get EXTDPD record
   //******************************************************************** 
   private Optional<DBContainer> findEXTDPD(int CONO, String DIVI, int DPID) {  
      DBAction query = database.table("EXTDPD").index("00").selection("EXNVBF").build()
      DBContainer EXTDPD = query.getContainer()
      EXTDPD.set("EXCONO", CONO)
      EXTDPD.set("EXDIVI", DIVI)
      EXTDPD.set("EXDPID", DPID)
      if(query.read(EXTDPD))  { 
        return Optional.of(EXTDPD)
      } 

      return Optional.empty()
   }


   //******************************************************************** 
   // Get EXTDPH record
   //******************************************************************** 
   private Optional<DBContainer> findEXTDPH(int CONO, String DIVI, int DPID){  
      DBAction query = database.table("EXTDPH").index("00").build()
      DBContainer EXTDPH = query.getContainer()
      EXTDPH.set("EXCONO", CONO)
      EXTDPH.set("EXDIVI", DIVI)
      EXTDPH.set("EXDPID", DPID)
      if(query.read(EXTDPH))  { 
        return Optional.of(EXTDPH)
      } 

      return Optional.empty()
   }



  //******************************************************************** 
  // Get all transactions from input deck
  //********************************************************************  
  private List<DBContainer> listEXTDPR(int CONO, String DIVI, int DPID){
    List<DBContainer>recLineEXTDPR = new ArrayList() 
    ExpressionFactory expression = database.getExpressionFactory("EXTDPR")
    expression = expression.eq("EXCONO", String.valueOf(CONO)).and(expression.eq("EXDIVI", DIVI)).and(expression.eq("EXDPID", String.valueOf(DPID)))
    
    DBAction query = database.table("EXTDPR").index("00").matching(expression).selection("EXDPID", "EXTNBF", "EXTRNB", "EXTRNO").reverse().build()
    DBContainer EXTDPR = query.createContainer()
    EXTDPR.set("EXCONO", CONO)
    EXTDPR.set("EXDIVI", DIVI)
    EXTDPR.set("EXDPID", DPID)

    int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords() 
    query.readAll(EXTDPR, 3, pageSize, { DBContainer recordEXTDPR ->  
       recLineEXTDPR.add(recordEXTDPR.createCopy()) 
    })

    return recLineEXTDPR
  }


  //******************************************************************** 
  // Update EXTDPH record
  //********************************************************************    
  void updEXTDPHRecord(){      
     DBAction action = database.table("EXTDPH").index("00").build()
     DBContainer EXTDPH = action.getContainer()     
     EXTDPH.set("EXCONO", inCONO)
     EXTDPH.set("EXDIVI", inDIVI)
     EXTDPH.set("EXDPID", inDPID)

     // Read with lock
     action.readLock(EXTDPH, updateCallBackEXTDPH)
     }
   
     Closure<?> updateCallBackEXTDPH = { LockedResult lockedResult -> 
       if (resultDPD != null && resultDPD != "") {
          lockedResult.set("EXISDB", resultDPD)
       }
  
       // Update changed information
       int changeNo = lockedResult.get("EXCHNO")
       int newChangeNo = changeNo + 1 
       int changeddate = utility.call("DateUtil", "currentDateY8AsInt")
       lockedResult.set("EXLMDT", changeddate)          
       lockedResult.set("EXCHNO", newChangeNo) 
       lockedResult.set("EXCHID", program.getUser())
       lockedResult.update()
    }

     
} 

