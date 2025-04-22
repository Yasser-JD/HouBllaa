package com.jdcoding.houbllaa.data

import com.jdcoding.houbllaa.models.PregnancyWeekInfo

/**
 * Static data provider for pregnancy timeline information by week
 */
object PregnancyTimelineData {
    
    /**
     * Get the complete list of pregnancy weeks with descriptions
     * @param currentWeek The current week of pregnancy to highlight
     */
    fun getPregnancyWeeks(currentWeek: Int): List<PregnancyWeekInfo> {
        return pregnancyWeeks.map { 
            it.copy(isCurrentWeek = it.week == currentWeek) 
        }
    }
    
    private val pregnancyWeeks = listOf(
        PregnancyWeekInfo(
            week = 1,
            title = "Week 1",
            description = "This first week is actually your menstrual period. Because your expected birth date (EDD or EDB) is calculated from the first day of your last period."
        ),
        PregnancyWeekInfo(
            week = 2,
            title = "Week 2",
            description = "Your body prepares for ovulation. The lining of your uterus starts getting thick and spongy again and your ovaries prepare to release an egg."
        ),
        PregnancyWeekInfo(
            week = 3,
            title = "Week 3",
            description = "Fertilization occurs! If a sperm cell meets your egg in the fallopian tube, they may combine and the fertilized egg will start dividing into multiple cells."
        ),
        PregnancyWeekInfo(
            week = 4,
            title = "Week 4",
            description = "The fertilized egg, now a blastocyst, implants into your uterine lining. You might notice some spotting or light bleeding as it burrows in."
        ),
        PregnancyWeekInfo(
            week = 5,
            title = "Week 5",
            description = "Your baby's heart, brain, and spinal cord are starting to develop. Most pregnancy tests will show positive now as hCG hormone rises."
        ),
        PregnancyWeekInfo(
            week = 6,
            title = "Week 6",
            description = "Your baby is about the size of a sweet pea. The neural tube, which will become the brain and spinal cord, is forming. A tiny heartbeat might be visible on ultrasound."
        ),
        PregnancyWeekInfo(
            week = 7,
            title = "Week 7",
            description = "Your baby is now the size of a blueberry. Small buds that will become arms and legs are forming, and the baby's facial features are developing."
        ),
        PregnancyWeekInfo(
            week = 8,
            title = "Week 8", 
            description = "Baby is now the size of a kidney bean. All essential organs are forming, and tiny fingers and toes are developing. Morning sickness may peak around now."
        ),
        PregnancyWeekInfo(
            week = 9,
            title = "Week 9",
            description = "Baby is about the size of a grape. The embryo is now officially a fetus. Tiny earlobes are forming, and the baby can move, though you won't feel it yet."
        ),
        PregnancyWeekInfo(
            week = 10,
            title = "Week 10",
            description = "Baby is the size of a kumquat. Vital organs are fully formed and functioning. The baby's limbs can bend, and tiny nails begin to form."
        ),
        PregnancyWeekInfo(
            week = 11,
            title = "Week 11",
            description = "Baby is the size of a fig. The baby's head is still large compared to the body, but the body is growing quickly. The placenta is fully formed."
        ),
        PregnancyWeekInfo(
            week = 12,
            title = "Week 12",
            description = "Baby is the size of a lime. The first trimester is almost complete! The baby's digestive system begins working, and reflexes are developing."
        ),
        PregnancyWeekInfo(
            week = 13,
            title = "Week 13 - Second Trimester Begins",
            description = "Baby is the size of a peapod. Fingerprints are forming, and the baby can make facial expressions. Your risk of miscarriage drops significantly."
        ),
        PregnancyWeekInfo(
            week = 14,
            title = "Week 14",
            description = "Baby is the size of a lemon. The baby's kidneys are working and producing urine. You might start feeling more energetic as first-trimester symptoms ease."
        ),
        PregnancyWeekInfo(
            week = 15,
            title = "Week 15",
            description = "Baby is the size of an apple. The baby's skeleton is developing, and the ears have moved into their final position. Hair begins to grow on the head."
        ),
        PregnancyWeekInfo(
            week = 16,
            title = "Week 16", 
            description = "Baby is the size of an avocado. The baby's eyes are working and can perceive light. You might start feeling the first flutters of movement."
        ),
        PregnancyWeekInfo(
            week = 17,
            title = "Week 17",
            description = "Baby is the size of a turnip. Baby's body fat begins developing, and sweat glands are forming. Your uterus is growing and shifting."
        ),
        PregnancyWeekInfo(
            week = 18,
            title = "Week 18",
            description = "Baby is the size of a bell pepper. The baby's genitals are formed, and an ultrasound might reveal the gender. Baby's movements become more coordinated."
        ),
        PregnancyWeekInfo(
            week = 19,
            title = "Week 19",
            description = "Baby is the size of a mango. The baby's senses are developing further, and the permanent teeth buds are forming behind the milk teeth buds."
        ),
        PregnancyWeekInfo(
            week = 20,
            title = "Week 20 - Halfway Point!",
            description = "Baby is the size of a banana. The baby is developing a regular sleep-wake cycle and can hear sounds from outside the womb."
        ),
        PregnancyWeekInfo(
            week = 21,
            title = "Week 21",
            description = "Baby is the size of a carrot. The baby's taste buds are fully formed, and bone marrow starts making blood cells."
        ),
        PregnancyWeekInfo(
            week = 22,
            title = "Week 22",
            description = "Baby is the size of a spaghetti squash. The baby's lips, eyebrows, and eyelids are more distinct. You might feel stronger movements."
        ),
        PregnancyWeekInfo(
            week = 23,
            title = "Week 23",
            description = "Baby is the size of a large mango. The baby can recognize your voice and may respond to it. Nipples form on the baby's chest."
        ),
        PregnancyWeekInfo(
            week = 24,
            title = "Week 24",
            description = "Baby is the size of an ear of corn. The baby's lungs are developing and producing surfactant, which will help them breathe after birth."
        ),
        PregnancyWeekInfo(
            week = 25,
            title = "Week 25",
            description = "Baby is the size of a rutabaga. The baby's hands can fully open and close, and the baby may start responding to familiar sounds."
        ),
        PregnancyWeekInfo(
            week = 26,
            title = "Week 26",
            description = "Baby is the size of a scallion. The baby's eyes open, and eyelashes form. Brain wave activity for hearing and sight becomes more regular."
        ),
        PregnancyWeekInfo(
            week = 27,
            title = "Week 27 - Third Trimester Begins",
            description = "Baby is the size of a cauliflower. The baby can hiccup, which you might feel as rhythmic movements. The baby may recognize your voice."
        ),
        PregnancyWeekInfo(
            week = 28,
            title = "Week 28",
            description = "Baby is the size of an eggplant. The baby can blink and is growing more fat. If born now, the baby would have a good chance of survival."
        ),
        PregnancyWeekInfo(
            week = 29,
            title = "Week 29",
            description = "Baby is the size of a butternut squash. The baby's muscles and lungs continue developing. Brain development is advancing rapidly."
        ),
        PregnancyWeekInfo(
            week = 30,
            title = "Week 30",
            description = "Baby is the size of a large cabbage. The baby's brain is developing specialized areas for different functions. Bone marrow is completely in charge of red blood cell production."
        ),
        PregnancyWeekInfo(
            week = 31,
            title = "Week 31",
            description = "Baby is the size of a coconut. The baby's irises can now dilate and contract in response to light. Most babies are in a head-down position by now."
        ),
        PregnancyWeekInfo(
            week = 32,
            title = "Week 32",
            description = "Baby is the size of a jicama. The baby's toenails are formed, and the baby is practicing breathing movements. If born now, the baby has a very high chance of survival."
        ),
        PregnancyWeekInfo(
            week = 33,
            title = "Week 33",
            description = "Baby is the size of a pineapple. The baby's bones are hardening, except for the skull, which remains soft for delivery. The baby's immune system is developing."
        ),
        PregnancyWeekInfo(
            week = 34,
            title = "Week 34",
            description = "Baby is the size of a cantaloupe. The baby's central nervous system and lungs are maturing rapidly. Most babies turn head-down in preparation for birth."
        ),
        PregnancyWeekInfo(
            week = 35,
            title = "Week 35",
            description = "Baby is the size of a honeydew melon. The baby's kidneys are fully developed, and the liver can process some waste products. Most of the baby's major development is complete."
        ),
        PregnancyWeekInfo(
            week = 36,
            title = "Week 36",
            description = "Baby is the size of a romaine lettuce. The baby is considered 'early term' now. The baby's lungs are nearly fully developed, preparing for first breath."
        ),
        PregnancyWeekInfo(
            week = 37,
            title = "Week 37 - Full Term!",
            description = "Baby is the size of a head of romaine lettuce. The baby is considered full term! The baby is likely in the head-down position, preparing for birth."
        ),
        PregnancyWeekInfo(
            week = 38,
            title = "Week 38",
            description = "Baby is the size of a leek. The baby's brain is still rapidly developing. The baby is practicing breathing movements and may have a firm grasp."
        ),
        PregnancyWeekInfo(
            week = 39,
            title = "Week 39",
            description = "Baby is the size of a mini watermelon. The baby's lungs continue to develop and mature. The baby's reflexes are coordinated, and grasp is strong."
        ),
        PregnancyWeekInfo(
            week = 40,
            title = "Week 40 - Due Date!",
            description = "Baby is the size of a small pumpkin. Your baby is ready to meet you! Most babies are born within a week before or after their due date."
        ),
        PregnancyWeekInfo(
            week = 41,
            title = "Week 41",
            description = "Baby is the size of a watermelon. Your healthcare provider will monitor you and baby closely. Most babies are born by the end of this week."
        ),
        PregnancyWeekInfo(
            week = 42,
            title = "Week 42",
            description = "Baby is the size of a watermelon. This is considered post-term. Your healthcare provider will likely recommend inducing labor if it hasn't started naturally."
        )
    )
}
